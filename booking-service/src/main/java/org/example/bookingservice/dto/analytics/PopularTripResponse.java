package org.example.bookingservice.dto.analytics;

import java.util.UUID;

public record PopularTripResponse(
    UUID tripId,
    Long bookingCount,
    Long totalSeats
) {}
