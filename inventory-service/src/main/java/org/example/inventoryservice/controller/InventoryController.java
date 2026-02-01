package org.example.inventoryservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.inventoryservice.dto.response.SeatResponse;
import org.example.inventoryservice.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory Controller", description = "API for managing inventory and seats")
public class InventoryController {

    private final InventoryService inventoryService;

    @Operation(summary = "Get seats for a trip", description = "Retrieves a list of seats for a specific trip ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Seats retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Trip not found")
    })
    @GetMapping("/trips/{tripId}/seats")
    public ResponseEntity<List<SeatResponse>> getTripSeats(@PathVariable UUID tripId) {
        return ResponseEntity.ok(inventoryService.getSeatsByTripId(tripId));
    }
}
