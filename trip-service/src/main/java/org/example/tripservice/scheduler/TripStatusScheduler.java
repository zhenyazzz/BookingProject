package org.example.tripservice.scheduler;

import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.kafka.core.KafkaTemplate;
import org.example.tripservice.repository.TripRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.example.kafka.event.TripDepartedEvent;
import org.example.kafka.event.TripArrivedEvent;
import java.util.UUID;
import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TripStatusScheduler {

    private final TripRepository tripRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Scheduled(fixedRate = 60000) 
    public void updateTrips() {
        LocalDateTime now = LocalDateTime.now();

        handleDepartures(now);
        handleArrivals(now);
    }

    @Transactional
    public void handleDepartures(LocalDateTime now) {
        List<UUID> ids = tripRepository.findDepartingTripIds(now);

        if (ids.isEmpty()) return;

        tripRepository.markInProgress(now);

        for (UUID id : ids) {
            kafkaTemplate.send("trip.departed", id.toString(), new TripDepartedEvent(UUID.randomUUID(), id, Instant.now()));
        }
    }

    @Transactional
    public void handleArrivals(LocalDateTime now) {

        List<UUID> ids = tripRepository.findArrivingTripIds(now);

        if (ids.isEmpty()) {
            return;
        }

        tripRepository.markCompleted(now);

        for (UUID id : ids) {
            kafkaTemplate.send(
                "trip.arrived",
                id.toString(),
                new TripArrivedEvent(
                    UUID.randomUUID(),
                    id,
                    Instant.now()
                )
            );
        }
    }
}


    

