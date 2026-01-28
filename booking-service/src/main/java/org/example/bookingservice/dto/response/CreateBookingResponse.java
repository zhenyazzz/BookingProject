package org.example.bookingservice.dto.response;

import org.example.bookingservice.model.BookingStatus;
import java.time.Instant;
import java.util.UUID;

public record CreateBookingResponse(
    UUID bookingId,
    UUID orderId,
    UUID tripId,
    int seatsCount,
    BookingStatus status,
    String paymentUrl,
    Instant reservationExpiresAt,
    Instant createdAt
) {
}
