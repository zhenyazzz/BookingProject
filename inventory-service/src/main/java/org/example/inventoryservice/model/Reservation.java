package org.example.inventoryservice.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record Reservation(
    UUID reservationId,
    UUID tripId,
    List<Integer> seatNumbers,
    Instant expiresAt
) {}

