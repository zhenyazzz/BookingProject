package org.example.bookingservice.client.payment;

import java.util.UUID;

public record CreatePaymentRequest(
    UUID orderId
) {}

