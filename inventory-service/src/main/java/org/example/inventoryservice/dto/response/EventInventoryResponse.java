package org.example.inventoryservice.dto.response;

import org.example.inventoryservice.model.Venue;

public record EventInventoryResponse(Long id, String name, Long capacity, VenueInventoryResponse venue, Long ticketPrice) {

}
