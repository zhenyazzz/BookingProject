package org.example.bookingservice.client.order;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateOrderRequest(
    UUID userId,
    UUID tripId,
    UUID reservationId,
    BigDecimal price,
    int seatsCount
) {}

