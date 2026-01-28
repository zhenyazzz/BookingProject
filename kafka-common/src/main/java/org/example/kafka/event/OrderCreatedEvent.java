package org.example.kafka.event;

import java.time.Instant;
import java.util.UUID;

public record OrderCreatedEvent(
    UUID eventId,
    UUID orderId,
    Instant createdAt
) {}

