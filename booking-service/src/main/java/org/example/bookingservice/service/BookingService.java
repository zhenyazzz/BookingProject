package org.example.bookingservice.service;

import com.booking.inventory.grpc.ReserveSeatsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bookingservice.client.inventory.InventoryGrpcClient;
import org.example.bookingservice.client.order.OrderClient;
import org.example.bookingservice.client.order.CreateOrderRequest;
import org.example.bookingservice.client.order.OrderResponse;
import org.example.bookingservice.client.payment.PaymentClient;
import org.example.bookingservice.client.payment.CreatePaymentRequest;
import org.example.bookingservice.client.payment.CreatePaymentResponse;
import org.example.bookingservice.client.trip.TripClient;
import org.example.bookingservice.client.trip.TripResponse;
import org.example.bookingservice.client.trip.TripStatus;
import org.example.bookingservice.dto.request.BookingRequest;
import org.example.bookingservice.dto.response.BookingResponse;
import org.example.bookingservice.dto.response.CreateBookingResponse;
import org.example.bookingservice.exception.BookingNotFoundException;
import org.example.bookingservice.exception.NotEnoughCapacityException;
import org.example.bookingservice.model.Booking;
import org.example.bookingservice.model.BookingStatus;
import org.example.bookingservice.repository.BookingRepository;
import org.example.bookingservice.util.SecurityUtils;
import org.example.bookingservice.mapper.BookingMapper;
import org.example.kafka.event.BookingFailedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final InventoryGrpcClient inventoryClient;
    private final OrderClient orderClient;
    private final PaymentClient paymentClient;
    private final TripClient tripClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public CreateBookingResponse createBooking(BookingRequest request) {
        UUID userId = SecurityUtils.currentUserId();
        log.info("Creating booking for user: {}, trip: {}, seats: {}", 
                userId, request.tripId(), request.seatsCount());

        TripResponse trip = fetchTrip(request.tripId());
        validateTripIsBookable(trip);
    
        Booking booking = createInitialBooking(userId, request);
        
        ReserveSeatsResponse reserveResponse = reserveSeats(booking, request.seatNumbers());
        
        UUID reservationId = UUID.fromString(reserveResponse.getReservationId());
        Instant expiresAt = Instant.parse(reserveResponse.getExpiresAt());
        booking.reserveSeats(reservationId, expiresAt);
        bookingRepository.save(booking);

        BigDecimal tripPrice = trip.price();
    
        OrderResponse orderResponse = createOrder(booking, reservationId, tripPrice);
        UUID orderId = orderResponse.id();
        
        booking.waitForPayment(orderId);
        bookingRepository.save(booking);
        CreatePaymentRequest createPaymentRequest = new CreatePaymentRequest(
                orderId, 
                orderResponse.totalPrice(), 
                "usd", 
                "Payment for order #" + orderId
        );
        String paymentUrl = initiatePayment(booking, createPaymentRequest, reservationId);
    
        return bookingMapper.toCreateResponse(booking, orderId, paymentUrl, expiresAt);
    }

    private void validateTripIsBookable(TripResponse trip) {
        if (trip.status() != TripStatus.SCHEDULED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Trip is not available for booking. Current status: " + trip.status()
            );
        }
    }

    private TripResponse fetchTrip(UUID tripId) {
        try {
            TripResponse trip = tripClient.getTrip(tripId);
            if (trip == null) {
                throw new RuntimeException("Trip not found: " + tripId);
            }
            return trip;
        } catch (Exception ex) {
            log.error("Failed to fetch trip: tripId={}", tripId, ex);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to fetch trip information"
            );
        }
    }

    private String initiatePayment(Booking booking, CreatePaymentRequest createPaymentRequest, UUID reservationId) {
        try {
            CreatePaymentResponse paymentResponse = paymentClient.createPayment(createPaymentRequest);
            String paymentUrl = paymentResponse.paymentUrl();
            log.debug("Payment URL created: {}", paymentUrl);
            return paymentUrl;
        } catch (Exception ex) {
            log.error("Failed to create payment for order: {}", createPaymentRequest.orderId(), ex);
            
            handleBookingFailure(booking);
            
            BookingFailedEvent event = new BookingFailedEvent(
                UUID.randomUUID(),
                booking.getId(),
                createPaymentRequest.orderId(),
                reservationId,
                "Failed to create payment: " + ex.getMessage(),
                Instant.now()
            );
            kafkaTemplate.send("booking.failed", booking.getId().toString(), event);
            
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to create payment for order: " + createPaymentRequest.orderId()
            );
        }
    }

    private void handleBookingFailure(Booking booking) {
        booking.cancel();
        bookingRepository.save(booking);
    }

    private Booking createInitialBooking(UUID userId, BookingRequest request) {
        Booking booking = Booking.builder()
                .userId(userId)
                .tripId(request.tripId())
                .seatsCount(request.seatsCount())
                .status(BookingStatus.CREATED)
                .build();
        return bookingRepository.save(booking);
    }

    private ReserveSeatsResponse reserveSeats(Booking booking, List<Integer> seatNumbers) {
        try {
            ReserveSeatsResponse response = inventoryClient.reserveSeats(
                    booking.getTripId(),
                    seatNumbers
            );
            log.debug("Seats reserved: reservationId={}, expiresAt={}", 
                    response.getReservationId(), response.getExpiresAt());
            return response;
        } catch (Exception ex) {
            log.error("Failed to reserve seats for trip: {}", booking.getTripId(), ex);
            handleBookingFailure(booking);
            throw new NotEnoughCapacityException(
                    "Not enough available seats for trip: " + booking.getTripId()
            );
        }
    }

    private OrderResponse createOrder(Booking booking, UUID reservationId, BigDecimal price) {
        try {
            CreateOrderRequest orderRequest = new CreateOrderRequest(
                    booking.getTripId(),
                    reservationId,
                    price,
                    booking.getSeatsCount()
            );
            OrderResponse response = orderClient.createOrder(orderRequest);
            log.debug("Order created: orderId={}", response.id());
            return response;
        } catch (Exception ex) {
            log.error("Failed to create order for booking: {}", booking.getId(), ex);
            
            handleBookingFailure(booking);
            
            BookingFailedEvent event = new BookingFailedEvent(
                UUID.randomUUID(),
                booking.getId(),
                null,
                reservationId,
                "Failed to create order: " + ex.getMessage(),
                Instant.now()
            );
            kafkaTemplate.send("booking.failed", booking.getId().toString(), event);
            
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to create order"
            );
        }
    }

    public BookingResponse getBookingById(UUID id) {
        return bookingRepository.findById(id)
                .map(booking -> bookingMapper.toResponse(booking))
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with id: " + id));
    }

    public Page<BookingResponse> getAllBookings(UUID userId, UUID tripId, BookingStatus status, Pageable pageable) {
        Specification<Booking> spec = (root, query, cb) -> cb.conjunction();

        if (userId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("userId"), userId));
        }
        if (tripId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("tripId"), tripId));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        Page<Booking> bookings = bookingRepository.findAll(spec, pageable);
        return bookings.map(bookingMapper::toResponse);
    }

    @Transactional
    public BookingResponse cancelBooking(UUID id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with id: " + id));

        UUID currentUserId = SecurityUtils.currentUserId();
        boolean isOwner = booking.getUserId().equals(currentUserId);
        boolean isAdmin = SecurityUtils.currentUserHasRole("ADMIN");
        if (!isOwner && !isAdmin) {
            log.warn("Cancel denied: bookingId={} belongs to user {}, current user {}", id, booking.getUserId(), currentUserId);
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can only cancel your own booking"
            );
        }
        
        log.info("Cancelling booking: bookingId={}, orderId={}, reservationId={}", 
                booking.getId(), booking.getOrderId(), booking.getReservationId());
        
        if (booking.getReservationId() != null) {
            try {
                inventoryClient.releaseReservation(booking.getReservationId());
                log.debug("Seats released: reservationId={}", booking.getReservationId());
            } catch (Exception ex) {
                log.error("Failed to release reservation: {}", booking.getReservationId(), ex);
            }
        }
        
        if (booking.getOrderId() != null) {
            try {
                orderClient.cancelOrder(booking.getOrderId());
                log.debug("Order cancelled: orderId={}", booking.getOrderId());
            } catch (Exception ex) {
                log.error("Failed to cancel order: {}", booking.getOrderId(), ex);
            }
        }
        
        booking.cancel();
        bookingRepository.save(booking);
        
        log.info("Booking cancelled successfully: bookingId={}", booking.getId());
        return bookingMapper.toResponse(booking);
    }

    @Transactional
    public void handleOrderCancelled(UUID orderId) {
        log.info("Handling order cancellation event: orderId={}", orderId);
        bookingRepository.findByOrderId(orderId)
                .ifPresentOrElse(
                        booking -> {
                            
                            if (booking.getStatus() != BookingStatus.CANCELLED) {
                                booking.cancel();
                                bookingRepository.save(booking);
                                log.info("Booking status updated to CANCELLED: bookingId={}", booking.getId());
                            } else {
                                log.debug("Booking already cancelled: bookingId={}", booking.getId());
                            }
                        },
                        () -> log.warn("Booking not found for orderId: {}", orderId)
                );
    }

    @Transactional
    public void handlePaymentSuccess(UUID orderId) {
        log.info("Handling payment success event: orderId={}", orderId);
        bookingRepository.findByOrderId(orderId)
                .ifPresentOrElse(
                        booking -> {
                            if (booking.getStatus() != BookingStatus.CONFIRMED) {
                                booking.confirm();
                                bookingRepository.save(booking);
                                log.info("Booking status updated to CONFIRMED: bookingId={}", booking.getId());
                            } else {
                                log.debug("Booking already confirmed: bookingId={}", booking.getId());
                            }
                        },
                        () -> log.warn("Booking not found for orderId: {}", orderId)
                );
    }


    @Transactional
    public void handlePaymentFailed(UUID orderId) {
        log.info("Handling payment failure event: orderId={}", orderId);
        bookingRepository.findByOrderId(orderId)
                .ifPresentOrElse(
                        booking -> {
                            if (booking.getStatus() != BookingStatus.CANCELLED) {
                                booking.cancel();
                                bookingRepository.save(booking);
                                log.info("Booking status updated to CANCELLED due to payment failure: bookingId={}", 
                                        booking.getId());
                            } else {
                                log.debug("Booking already cancelled: bookingId={}", booking.getId());
                            }
                        },
                        () -> log.warn("Booking not found for orderId: {}", orderId)
                );
    }
}