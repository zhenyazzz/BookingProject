package org.example.tripservice.scheduler;

import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;
import org.example.tripservice.repository.TripRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.example.kafka.event.TripDepartedEvent;
import org.example.kafka.event.EventType;
import org.example.kafka.event.TripArrivedEvent;
import java.util.UUID;
import java.time.Instant;
import java.util.List;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.example.tripservice.service.OutboxService;

@Component
@RequiredArgsConstructor
@Slf4j
public class TripStatusScheduler {

    private final TripRepository tripRepository;
    private final OutboxService outboxService;

    @Scheduled(fixedRate = 60000) 
    @Transactional
    public void updateTrips() {
        LocalDateTime now = LocalDateTime.now();

        handleDepartures(now);
        handleArrivals(now);
    }

    public void handleDepartures(LocalDateTime now) {
        List<UUID> ids = tripRepository.markInProgressAndReturnIds(now);
    
        if (ids.isEmpty()) {
            return;
        }
    
        for (UUID id : ids) {
            UUID eventId = generateEventId(id, EventType.TRIP_DEPARTED);
            TripDepartedEvent event = new TripDepartedEvent(eventId, id, Instant.now());
            outboxService.saveEvent(id, EventType.TRIP_DEPARTED, event);
        }
    }

    public void handleArrivals(LocalDateTime now) {
        List<UUID> ids = tripRepository.markCompletedAndReturnIds(now);
    
        if (ids.isEmpty()) {
            return;
        }
    
        for (UUID id : ids) {
            UUID eventId = generateEventId(id, EventType.TRIP_ARRIVED);
            TripArrivedEvent event = new TripArrivedEvent(eventId, id, Instant.now());
            outboxService.saveEvent(id, EventType.TRIP_ARRIVED, event);
        }
    }

    private UUID generateEventId(UUID tripId, EventType eventType) {
        String source = tripId.toString() + ":" + eventType.name();
        return UUID.nameUUIDFromBytes(source.getBytes(StandardCharsets.UTF_8));
    }
}


    

