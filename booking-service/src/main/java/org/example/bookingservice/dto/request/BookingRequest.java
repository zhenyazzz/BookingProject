package org.example.bookingservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record BookingRequest(
        @NotNull(message = "Trip ID is required")
        UUID tripId,

        @Min(value = 1, message = "Seats count must be at least 1")
        int seatsCount
) {
}
