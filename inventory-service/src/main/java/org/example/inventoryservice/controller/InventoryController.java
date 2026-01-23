package org.example.inventoryservice.controller;

import java.util.List;

import org.example.inventoryservice.dto.response.EventInventoryResponse;
import org.example.inventoryservice.dto.response.VenueInventoryResponse;
import org.example.inventoryservice.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/events")
    public @ResponseBody List<EventInventoryResponse> inventoryGetAllEvents(){
        return inventoryService.getAllEvents();
    }
    @GetMapping("/venues")
    public @ResponseBody List<VenueInventoryResponse> inventoryGetAllVenues(){
        return inventoryService.getAllVenues();
    }
    @GetMapping("/venue/{venueId}")
    public @ResponseBody VenueInventoryResponse getVenueById(@PathVariable Long venueId) {
        return inventoryService.getVenueInformation(venueId);
    }
    @GetMapping("/event/{eventId}")
    public @ResponseBody EventInventoryResponse getEventById(@PathVariable Long eventId) {
        return inventoryService.getEventInformation(eventId);
    }

    @PutMapping("/event/{eventId}/capacity/{capacity}")
    public ResponseEntity<EventInventoryResponse> updateEventInventory(
            @PathVariable Long eventId,
            @PathVariable Long capacity) {
        EventInventoryResponse response = inventoryService.updateEventCapacity(eventId, capacity);
        return ResponseEntity.ok(response);
    }


}
