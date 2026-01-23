package org.example.bookingservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bookingservice.dto.request.BookingRequest;
import org.example.bookingservice.dto.response.BookingResponse;
import org.example.bookingservice.exception.BookingNotFoundException;
import org.example.bookingservice.model.Booking;
import org.example.bookingservice.model.BookingStatus;
import org.example.bookingservice.repository.BookingRepository;
import org.example.bookingservice.util.SecurityUtils;
import org.example.bookingservice.mapper.BookingMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;

    public BookingResponse createBooking(BookingRequest request) {

        UUID userId = SecurityUtils.currentUserId();
    
        Booking booking = bookingRepository.save(
                Booking.builder()
                        .userId(userId)
                        .tripId(request.tripId())
                        .seatsCount(request.seatsCount())
                        .status(BookingStatus.CREATED)
                        .build()
        );
    
        ReserveSeatsResponse reserveResponse;
        try {
            reserveResponse = inventoryClient.reserveSeats(
                    booking.getTripId(),
                    booking.getSeatsCount()
            );
        } catch (SeatsNotAvailableException ex) {
            booking.cancel();
            bookingRepository.save(booking);
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Not enough available seats"
            );
        }
    
        booking.reserveSeats(
                reserveResponse.reservationId(),
                reserveResponse.expiresAt()
        );
        bookingRepository.save(booking);
    
        UUID orderId = orderClient.createOrder(
                booking.getId(),
                booking.getTripId(),
                booking.getSeatsCount(),
                userId
        );
    
        booking.waitForPayment(orderId);
        bookingRepository.save(booking);
    
        String paymentUrl = paymentClient.createPayment(orderId);
    
        return bookingMapper.toResponse(booking, paymentUrl);
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
        return bookingRepository.findById(id)
                .map(booking -> {
                    booking.setStatus(BookingStatus.CANCELLED);
                    return bookingMapper.toResponse(bookingRepository.save(booking));
                })
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with id: " + id));
    }
}