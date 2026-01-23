package org.example.paymentservice.dto;

public record PaymentResponse(
        String paymentIntentId,
        String clientSecret,
        Long amount,
        String currency,
        String orderId
) {
}
