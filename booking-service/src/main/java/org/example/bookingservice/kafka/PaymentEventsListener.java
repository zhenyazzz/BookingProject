package org.example.bookingservice.kafka;

import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.example.bookingservice.service.BookingService;
import org.example.kafka.event.PaymentSucceededEvent;
import org.example.kafka.event.PaymentFailedEvent;

@Component
@RequiredArgsConstructor
public class PaymentEventsListener {

    private final BookingService bookingService;

    @KafkaListener(topics = "payment.succeeded", groupId = "booking-service")
    public void onPaymentSucceeded(PaymentSucceededEvent event) {
        bookingService.handlePaymentSuccess(event.orderId());
    }

    @KafkaListener(topics = "payment.failed", groupId = "booking-service")
    public void onPaymentFailed(PaymentFailedEvent event) {
        bookingService.handlePaymentFailed(event.orderId());
    }
}


