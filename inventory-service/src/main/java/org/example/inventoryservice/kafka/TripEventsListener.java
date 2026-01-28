package org.example.inventoryservice.kafka;

import org.example.inventoryservice.service.InventoryService;
import org.example.kafka.event.TripCreatedEvent;
import org.example.kafka.event.TripCancelledEvent;
import org.example.kafka.event.TripDepartedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TripEventsListener {

    private final InventoryService inventoryService;

    @KafkaListener(topics = "trip.created", groupId = "inventory-service")
    public void onTripCreated(TripCreatedEvent event) {
        inventoryService.handleTripCreated(event);
    }

    @KafkaListener(topics = "trip.cancelled", groupId = "inventory-service")
    public void onTripCancelled(TripCancelledEvent event) {
        inventoryService.handleTripCancelled(event);
    }

    @KafkaListener(topics = "trip.departed", groupId = "inventory-service")
    public void onTripDeparted(TripDepartedEvent event) {
        inventoryService.handleTripDeparted(event);
    }

}
