package org.example.tripservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.example.kafka.event.BusType;

public record TripCreateRequest(
    @NotNull(message = "Route ID is required")
    UUID routeId,

    @NotNull(message = "Departure time is required")
    @Future(message = "Departure time must be in the future")
    LocalDateTime departureTime,

    @NotNull(message = "Arrival time is required")
    @Future(message = "Arrival time must be in the future")
    LocalDateTime arrivalTime,

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
    BigDecimal price,

    @NotNull(message = "Bus type is required")
    BusType busType
) {}
