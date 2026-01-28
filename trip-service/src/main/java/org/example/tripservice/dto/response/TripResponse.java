package org.example.tripservice.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.example.tripservice.model.TripStatus;
import org.example.kafka.event.BusType;

public record TripResponse(
    UUID id,
    RouteResponse route,
    LocalDateTime departureTime,
    LocalDateTime arrivalTime,
    BigDecimal price,
    int totalSeats,
    TripStatus status,
    BusType busType
) {}

