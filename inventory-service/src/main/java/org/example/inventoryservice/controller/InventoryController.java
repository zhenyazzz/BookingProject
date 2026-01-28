package org.example.inventoryservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.inventoryservice.dto.response.SeatResponse;
import org.example.inventoryservice.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/trips/{tripId}/seats")
    public ResponseEntity<List<SeatResponse>> getTripSeats(@PathVariable UUID tripId) {
        return ResponseEntity.ok(inventoryService.getSeatsByTripId(tripId));
    }
}
