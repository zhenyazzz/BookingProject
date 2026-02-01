package org.example.kafka.event;

import java.time.Instant;
import java.util.UUID;

public record BookingFailedEvent(
    UUID eventId,
    UUID bookingId,
    UUID orderId,
    UUID reservationId,
    String reason,
    Instant timestamp
) {}
