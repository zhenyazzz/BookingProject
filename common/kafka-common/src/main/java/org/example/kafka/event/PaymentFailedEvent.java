package org.example.kafka.event;

import java.time.Instant;
import java.util.UUID;

public record PaymentFailedEvent(
    UUID eventId,
    UUID paymentId,
    UUID orderId,
    String reason,
    Instant failedAt
) {}
