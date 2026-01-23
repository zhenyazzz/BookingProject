package org.example.bookingservice.dto.response;

import org.example.bookingservice.model.BookingStatus;
import java.time.Instant;
import java.util.UUID;

public record BookingResponse(
    UUID id,
    UUID userId,
    UUID tripId,
    int seatsCount,
    BookingStatus status,
    Instant createdAt
) {
}
