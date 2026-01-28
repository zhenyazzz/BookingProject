package org.example.tripservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TripUpdateRequest(
    @Future(message = "Departure time must be in the future")
    LocalDateTime departureTime,

    @Future(message = "Arrival time must be in the future")
    LocalDateTime arrivalTime,

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
    BigDecimal price,

    UUID routeId
) {}
