package org.example.inventoryservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kafka.event.BookingFailedEvent;
import org.example.inventoryservice.service.InventoryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingEventListener {

    private final InventoryService inventoryService;

    @KafkaListener(topics = "booking.failed", groupId = "inventory-service")
    public void onBookingFailed(BookingFailedEvent event) {
        log.info("Received booking failed event: bookingId={}, reason={}", event.bookingId(), event.reason());
        
        if (event.reservationId() != null) {
            try {
                inventoryService.releaseReservation(event.reservationId());
                log.info("Released reservation for failed booking: reservationId={}", event.reservationId());
            } catch (Exception e) {
                log.error("Failed to release reservation for failed booking: reservationId={}", event.reservationId(), e);
            }
        } else {
            log.debug("No reservationId in booking failed event, skipping reservation release");
        }
    }
}
