package org.example.orderservice.dto;

import org.example.orderservice.model.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderResponse(
    UUID id,
    BigDecimal totalPrice,
    Integer seatsCount,
    UUID tripId,
    UUID userId,
    OrderStatus status,
    Instant createdAt
) {
}
