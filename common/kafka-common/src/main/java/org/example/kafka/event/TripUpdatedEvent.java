package org.example.kafka.event;

import java.time.Instant;
import java.util.UUID;

public record TripUpdatedEvent(
    UUID eventId,
    UUID tripId,
    BusType busType,
    Instant updatedAt
) {

}
