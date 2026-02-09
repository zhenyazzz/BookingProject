package org.example.orderservice.kafka;

import org.example.kafka.event.OrderCancelledEvent;
import org.example.kafka.event.OrderConfirmedEvent;
import org.example.kafka.event.OrderCreatedEvent;

public interface OrderEventPublisher {

    void publishOrderCreated(OrderCreatedEvent event);

    void publishOrderConfirmed(OrderConfirmedEvent event);

    void publishOrderCancelled(OrderCancelledEvent event);

    void sendToDlq(String key, Object payload);
}
