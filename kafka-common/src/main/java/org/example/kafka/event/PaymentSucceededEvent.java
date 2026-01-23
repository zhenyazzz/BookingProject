package org.example.kafka.event;

import java.util.UUID;
import java.time.Instant;

public record PaymentSucceededEvent(
    UUID eventId,
    UUID orderId,
    UUID paymentId,
    Instant paidAt
) {}
