package org.example.bookingservice.kafka;

import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.example.bookingservice.service.BookingService;
import org.example.kafka.event.OrderCancelledEvent;

@Component
@RequiredArgsConstructor
public class OrderEventsListener {
    private final BookingService bookingService;

    @KafkaListener(topics = "order.cancelled", groupId = "booking-service")
    public void onOrderCancelled(OrderCancelledEvent event) {
        bookingService.handleOrderCancelled(event.orderId());
    }

}
