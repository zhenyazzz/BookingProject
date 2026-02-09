package org.example.orderservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;


public record CreateOrderRequest(
    @NotNull(message = "Trip ID is required")
    UUID tripId,
    
    @NotNull(message = "Reservation ID is required")
    UUID reservationId,
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
    BigDecimal price,
    
    @Min(value = 1, message = "Seats count must be at least 1")
    int seatsCount
) {}

