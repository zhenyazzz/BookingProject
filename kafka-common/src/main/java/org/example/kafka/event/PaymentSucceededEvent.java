package org.example.kafka.event;

import java.time.Instant;
import java.util.UUID;

public record PaymentSucceededEvent(
    UUID eventId,
    UUID orderId,
    UUID paymentId,
    Instant paidAt
) {}
