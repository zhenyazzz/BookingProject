package org.example.orderservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kafka.event.BookingFailedEvent;
import org.example.orderservice.service.OrderService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingEventListener {

    private final OrderService orderService;

    @KafkaListener(topics = "booking.failed", groupId = "order-service")
    public void onBookingFailed(BookingFailedEvent event) {
        log.info("Received booking failed event: bookingId={}, reason={}", event.bookingId(), event.reason());
        
        if (event.orderId() != null) {
            try {
                orderService.cancelOrder(event.orderId());
                log.info("Cancelled order for failed booking: orderId={}", event.orderId());
            } catch (Exception e) {
                log.error("Failed to cancel order for failed booking: orderId={}", event.orderId(), e);
                // Throwing exception to retry via Kafka if Order Service is temporarily down (e.g. DB issue)
                throw e; 
            }
        } else {
            log.debug("No orderId in booking failed event, skipping order cancellation");
        }
    }
}
