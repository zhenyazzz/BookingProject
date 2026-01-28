package org.example.kafka.event;

import java.time.Instant;
import java.util.UUID;

public record ReservationExpiredEvent(
        UUID eventId,
        UUID reservationId,
        UUID tripId,
        Instant expiredAt
) {}

