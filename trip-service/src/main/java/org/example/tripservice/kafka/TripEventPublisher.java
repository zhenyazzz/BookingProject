package org.example.tripservice.kafka;

import org.example.kafka.event.TripArrivedEvent;
import org.example.kafka.event.TripCancelledEvent;
import org.example.kafka.event.TripCreatedEvent;
import org.example.kafka.event.TripDepartedEvent;

public interface TripEventPublisher {

    void publishTripCreated(TripCreatedEvent event);

    void publishTripCancelled(TripCancelledEvent event);

    void publishTripDeparted(TripDepartedEvent event);

    void publishTripArrived(TripArrivedEvent event);
}

