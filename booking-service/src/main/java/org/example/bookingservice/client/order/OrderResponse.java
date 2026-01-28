package org.example.bookingservice.client.order;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderResponse(
    UUID id,
    BigDecimal totalPrice,
    String status
) {}
