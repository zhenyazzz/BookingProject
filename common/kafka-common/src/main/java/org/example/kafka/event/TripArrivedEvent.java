package org.example.kafka.event;

import java.time.Instant;
import java.util.UUID;

public record TripArrivedEvent(
    UUID eventId,
    UUID tripId,
    Instant arrivedAt
) {

}
