package org.example.inventoryservice.dto.response;

import org.example.inventoryservice.model.SeatStatus;
import java.util.UUID;

public record SeatResponse(
    UUID id,
    int seatNumber,
    SeatStatus status
) {}
