package org.example.paymentservice.dto;

public record PaymentRequest(
        Long amount,
        Long quantity,
        String name,
        String currency
) {
}
