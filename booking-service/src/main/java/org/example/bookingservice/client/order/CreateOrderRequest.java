package org.example.bookingservice.client.order;

import java.util.UUID;

public record CreateOrderRequest(
    UUID userId,
    UUID tripId,
    int seatsCount
) {}

