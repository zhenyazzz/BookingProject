package org.example.orderservice.dto;

public record EventInventoryResponse(Long id, String name, Long capacity, VenueInventoryResponse venue, Long ticketPrice) {

}
