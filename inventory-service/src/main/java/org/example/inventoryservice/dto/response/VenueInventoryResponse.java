package org.example.inventoryservice.dto.response;

public record VenueInventoryResponse(
    Long id,
    String name,
    Long totalCapacity
) {

}
