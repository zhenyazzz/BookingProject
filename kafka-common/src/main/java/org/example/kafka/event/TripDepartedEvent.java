package org.example.kafka.event;

import java.time.Instant;
import java.util.UUID;

public record TripDepartedEvent(
    UUID eventId,
    UUID tripId,
    Instant departedAt
) {

}
