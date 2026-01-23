package org.example.inventoryservice.service;

import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.example.inventoryservice.dto.response.EventInventoryResponse;
import org.example.inventoryservice.dto.response.VenueInventoryResponse;
import org.example.inventoryservice.model.Event;
import org.example.inventoryservice.repository.EventRepository;
import org.example.inventoryservice.repository.VenueRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {
    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;
    public List<EventInventoryResponse> getAllEvents(){
        return eventRepository.findAll().stream()
        .map(event -> {
            return new EventInventoryResponse(event.getId(), event.getName(), event.getTotalCapacity(), new VenueInventoryResponse(event.getVenue().getId(), event.getVenue().getName(), event.getVenue().getTotalCapacity()), event.getTicketPrice());
        })
        .collect(Collectors.toList());
    }
    
    public VenueInventoryResponse getVenueInformation(Long venueId) {
        return venueRepository.findById(venueId).map(venue -> {
            return new VenueInventoryResponse(venue.getId(), venue.getName(), venue.getTotalCapacity());
        }).orElseThrow(() -> new RuntimeException("Venue not found"));
    }

    public EventInventoryResponse getEventInformation(Long eventId) {
        return eventRepository.findById(eventId).map(event -> {
            return new EventInventoryResponse(event.getId(), event.getName(), event.getTotalCapacity(), new VenueInventoryResponse(event.getVenue().getId(), event.getVenue().getName(), event.getVenue().getTotalCapacity()), event.getTicketPrice());
        }).orElseThrow(() -> new RuntimeException("Event not found"));
    }

    public EventInventoryResponse updateEventCapacity(Long eventId, Long capacity) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new RuntimeException("Event not found"));
        if (event.getTotalCapacity() < capacity) {
            throw new RuntimeException("Capacity exceeded");
        }
        event.setTotalCapacity(event.getTotalCapacity() - capacity);
        eventRepository.saveAndFlush(event);
        log.info("Updated event capacity");
        return new EventInventoryResponse(event.getId(), event.getName(), event.getTotalCapacity(), new VenueInventoryResponse(event.getVenue().getId(), event.getVenue().getName(), event.getVenue().getTotalCapacity()), event.getTicketPrice());
    }

    public List<VenueInventoryResponse> getAllVenues() {
        return venueRepository.findAll().stream()
        .map(venue -> {
            return new VenueInventoryResponse(venue.getId(), venue.getName(), venue.getTotalCapacity());
        })
        .collect(Collectors.toList());
    }
}
