package org.example.bookingservice.kafka;

import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.example.bookingservice.service.BookingService;
import org.example.kafka.event.ReservationExpiredEvent;
@Component
@RequiredArgsConstructor
public class ReservationEventsListener {
    private final BookingService bookingService;

    @KafkaListener(topics = "reservation.expired", groupId = "booking-service")
    public void onReservationExpired(ReservationExpiredEvent event) {
        bookingService.handleReservationExpired(event.reservationId());
    }

}
