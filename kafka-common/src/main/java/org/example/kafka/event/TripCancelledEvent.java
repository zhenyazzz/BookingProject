package org.example.kafka.event;

import java.time.Instant;
import java.util.UUID;

public record TripCancelledEvent(
    UUID eventId,
    UUID tripId,
    Instant cancelledAt
) {

}
