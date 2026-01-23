package org.example.orderservice.dto;

public record VenueInventoryResponse(
    Long id,
    String name,
    Long totalCapacity
) {

}
