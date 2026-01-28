package org.example.inventoryservice.service;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.example.kafka.event.TripCreatedEvent;
import org.example.kafka.event.TripDepartedEvent;
import org.example.kafka.event.TripCancelledEvent;
import org.example.inventoryservice.repository.SeatRepository;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.UUID;
import org.example.inventoryservice.model.Reservation;
import org.example.inventoryservice.model.Seat;
import org.example.inventoryservice.model.SeatStatus;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.ArrayList;

import java.util.concurrent.TimeUnit;
import org.example.kafka.event.ReservationExpiredEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.beans.factory.annotation.Value;
import java.time.Instant;
import java.time.LocalDateTime;

import org.example.inventoryservice.exception.NotEnoughSeatsException;

import org.example.inventoryservice.dto.response.SeatResponse;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {
    private final SeatRepository seatRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${reservation.data-prefix}")
    private String dataPrefix;

    @Value("${reservation.trigger-prefix}")
    private String triggerPrefix;

    @Value("${reservation.ttl-minutes}")
    private long reservationTtlMinutes;

    public List<SeatResponse> getSeatsByTripId(UUID tripId) {
        log.info("Fetching all seats for tripId: {}", tripId);
        return seatRepository.findByTripId(tripId).stream()
                .map(seat -> new SeatResponse(seat.getId(), seat.getSeatNumber(), seat.getStatus()))
                .collect(Collectors.toList());
    }

    @Transactional
    public Reservation reserveSeats(UUID tripId, List<Integer> seatNumbers) {
        log.info("Attempting to reserve seats {} for trip {}", seatNumbers, tripId);
        
        List<Seat> seats = seatRepository.findByTripIdAndSeatNumberIn(tripId, seatNumbers);
        
        if (seats.size() != seatNumbers.size()) {
            throw new NotEnoughSeatsException("Some seats not found");
        }
        
        for (Seat seat : seats) {
            if (seat.getStatus() != SeatStatus.AVAILABLE) {
                throw new NotEnoughSeatsException("Seat " + seat.getSeatNumber() + " is not available");
            }
            seat.setStatus(SeatStatus.RESERVED);
        }
        
        seatRepository.saveAll(seats);
        
        UUID reservationId = UUID.randomUUID();
        Reservation reservation = new Reservation(
            reservationId,
            tripId,
            seatNumbers,
            Instant.now().plusSeconds(reservationTtlMinutes * 60)
        );
        
        redisTemplate.opsForValue().set(dataPrefix + reservationId, reservation, reservationTtlMinutes * 2, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(triggerPrefix + reservationId, "", reservationTtlMinutes, TimeUnit.MINUTES);
        
        log.info("Seats reserved successfully. Reservation ID: {}", reservationId);
        return reservation;
    }

    @Transactional
    public void confirmReservation(UUID reservationId) {
        log.info("Confirming reservation {}", reservationId);
        Reservation reservation = (Reservation) redisTemplate.opsForValue().get(dataPrefix + reservationId);
        
        if (reservation == null) {
            throw new RuntimeException("Reservation not found or already expired");
        }
        
        List<Seat> seats = seatRepository.findByTripIdAndSeatNumberIn(reservation.tripId(), reservation.seatNumbers());
        for (Seat seat : seats) {
            seat.setStatus(SeatStatus.SOLD);
        }
        seatRepository.saveAll(seats);
        
        redisTemplate.delete(List.of(dataPrefix + reservationId, triggerPrefix + reservationId));
        log.info("Reservation {} confirmed and seats marked as SOLD", reservationId);
    }

    @Transactional
    public void releaseReservation(UUID reservationId) {
        log.info("Releasing reservation {}", reservationId);
        Reservation reservation = (Reservation) redisTemplate.opsForValue().get(dataPrefix + reservationId);
        
        if (reservation != null) {
            List<Seat> seats = seatRepository.findByTripIdAndSeatNumberIn(reservation.tripId(), reservation.seatNumbers());
            for (Seat seat : seats) {
                seat.setStatus(SeatStatus.AVAILABLE);
            }
            seatRepository.saveAll(seats);
            redisTemplate.delete(List.of(dataPrefix + reservationId, triggerPrefix + reservationId));
            log.info("Reservation {} released and seats marked as AVAILABLE", reservationId);
        }
    }

    @Transactional
    public void handleReservationExpiration(UUID reservationId) {
        log.info("Handling expiration for reservation {}", reservationId);
        Reservation reservation = (Reservation) redisTemplate.opsForValue().get(dataPrefix + reservationId);
        
        if (reservation != null) {
            List<Seat> seats = seatRepository.findByTripIdAndSeatNumberIn(reservation.tripId(), reservation.seatNumbers());
            for (Seat seat : seats) {
                if (seat.getStatus() == SeatStatus.RESERVED) {
                    seat.setStatus(SeatStatus.AVAILABLE);
                }
            }
            seatRepository.saveAll(seats);
            
            ReservationExpiredEvent event = new ReservationExpiredEvent(
                UUID.randomUUID(),
                reservationId,
                reservation.tripId(),
                Instant.now()
            );
            kafkaTemplate.send("reservation.expired", reservationId.toString(), event);
            
            redisTemplate.delete(dataPrefix + reservationId);
            log.info("Reservation {} expired, seats released and event sent", reservationId);
        }
    }

    @Transactional
    public void cleanupExpiredReservations() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(reservationTtlMinutes * 2);
        List<Seat> expiredSeats = seatRepository.findExpiredReservations(threshold);
        
        if (!expiredSeats.isEmpty()) {
            log.info("Found {} expired reservations in safety net. Releasing...", expiredSeats.size());
            for (Seat seat : expiredSeats) {
                seat.setStatus(SeatStatus.AVAILABLE);
                seat.setLastStatusUpdate(LocalDateTime.now());
            }
            seatRepository.saveAll(expiredSeats);
        }
    }

    @Transactional
    public void handleTripCreated(TripCreatedEvent event) {
        UUID tripId = event.tripId();

        if (seatRepository.existsByTripId(tripId)) {
            log.info("Seats already created for tripId: {}", tripId);
            return;
        }

        int capacity = event.busType().getCapacity();
        List<Seat> seats = new ArrayList<>(capacity);

        for (int i = 1; i <= capacity; i++) {
            Seat seat = new Seat();
            seat.setTripId(tripId);
            seat.setSeatNumber(i);
            seat.setStatus(SeatStatus.AVAILABLE);
            seats.add(seat);
        }

        seatRepository.saveAll(seats);
        log.info("Seats created for tripId: {}", tripId);
    }

    @Transactional
    public void handleTripCancelled(TripCancelledEvent event) {
        log.info("Updating seats status to CANCELLED for tripId: {}", event.tripId());
        seatRepository.updateStatusByTripId(event.tripId(), SeatStatus.CANCELLED);
    }

    @Transactional
    public void handleTripDeparted(TripDepartedEvent event) {
        log.info("Updating seats status to SOLD for tripId: {}", event.tripId());
        seatRepository.updateStatusByTripId(event.tripId(), SeatStatus.SOLD);
    }
}
