package org.example.bookingservice.client.payment;

public record CreatePaymentResponse(
    String paymentUrl,
    String status
) {}

