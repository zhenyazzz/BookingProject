package org.example.orderservice.kafka;

import org.example.orderservice.service.OrderService;
import org.example.kafka.event.ReservationExpiredEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class InventoryEventListener {

    private final OrderService orderService;

    @KafkaListener(topics = "reservation.expired", groupId = "order-service")
    public void onReservationExpired(ReservationExpiredEvent event) {
        orderService.handleReservationExpired(event);
    }

}
