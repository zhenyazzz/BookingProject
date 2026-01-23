package org.example.bookingservice.client.order;

import java.util.UUID;

public record OrderResponse(
    UUID orderId,
    String status
) {}

