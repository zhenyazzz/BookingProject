package org.example.orderservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kafka.event.PaymentFailedEvent;
import org.example.kafka.event.PaymentSucceededEvent;
import org.example.orderservice.service.OrderService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {

    private final OrderService orderService;

    @KafkaListener(topics = "payment.succeeded", groupId = "order-service")
    public void onPaymentSucceeded(PaymentSucceededEvent event) {
        log.info("Received payment succeeded event: orderId={}", event.orderId());
        orderService.handlePaymentSucceeded(event);
    }

    @KafkaListener(topics = "payment.failed", groupId = "order-service")
    public void onPaymentFailed(PaymentFailedEvent event) {
        log.info("Received payment failed event: orderId={}, reason={}", event.orderId(), event.reason());
        orderService.handlePaymentFailed(event);
    }
}
