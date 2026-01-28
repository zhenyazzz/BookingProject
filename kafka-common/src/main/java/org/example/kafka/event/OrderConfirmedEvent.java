package org.example.kafka.event;

import java.time.Instant;
import java.util.UUID;

public record OrderConfirmedEvent(
    UUID eventId,
    UUID orderId,
    Instant confirmedAt
) {}

