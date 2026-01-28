package org.example.bookingservice.client.trip;

import java.math.BigDecimal;
import java.util.UUID;

public record TripResponse(
    UUID id,
    BigDecimal price,
    TripStatus status
) {}
