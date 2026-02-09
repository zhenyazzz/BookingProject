package org.example.tripservice.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.example.kafka.event.TripArrivedEvent;
import org.example.kafka.event.TripCancelledEvent;
import org.example.kafka.event.TripCreatedEvent;
import org.example.kafka.event.TripDepartedEvent;
import org.example.tripservice.kafka.TripEventPublisher;
import org.example.tripservice.model.OutboxEvent;
import org.example.tripservice.model.OutboxStatus;
import org.example.tripservice.repository.OutboxEventRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxDispatcher {

    private final OutboxEventRepository outboxEventRepository;
    private final TripEventPublisher tripEventPublisher;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${trip.kafka.dlq-topic:trip.dead-letter}")
    private String dlqTopic;

    @Value("${trip.outbox.max-retries:5}")
    private int maxRetries;

    @Scheduled(fixedRate = 10000)
    @Transactional
    public void processOutboxEvents() {
        List<OutboxEvent> events = outboxEventRepository.findTop100NewForUpdateSkipLocked();

        if (events.isEmpty()) {
            return;
        }

        log.info("Processing {} outbox events", events.size());

        for (OutboxEvent event : events) {
            try {
                processEvent(event);
            } catch (Exception ex) {
                log.error("Failed to process outbox event: id={}, eventType={}", event.getId(), event.getEventType(), ex);
                handleFailedEvent(event, ex);
            }
        }
    }

    private void processEvent(OutboxEvent event) throws Exception {
        Object payload = deserializePayload(event);

        try {
            switch (event.getEventType()) {
                case TRIP_CREATED -> {
                    TripCreatedEvent tripEvent = (TripCreatedEvent) payload;
                    tripEventPublisher.publishTripCreated(tripEvent);
                }
                case TRIP_CANCELLED -> {
                    TripCancelledEvent tripEvent = (TripCancelledEvent) payload;
                    tripEventPublisher.publishTripCancelled(tripEvent);
                }
                case TRIP_DEPARTED -> {
                    TripDepartedEvent tripEvent = (TripDepartedEvent) payload;
                    tripEventPublisher.publishTripDeparted(tripEvent);
                }
                case TRIP_ARRIVED -> {
                    TripArrivedEvent tripEvent = (TripArrivedEvent) payload;
                    tripEventPublisher.publishTripArrived(tripEvent);
                }
                default -> {
                    log.warn("Unknown event type: {}", event.getEventType());
                    return;
                }
            }

            outboxEventRepository.markSent(event.getId(), OutboxStatus.SENT, LocalDateTime.now());
            log.info("Successfully processed outbox event: id={}, eventType={}", event.getId(), event.getEventType());
        } catch (Exception ex) {
            log.warn("Failed to publish event: id={}, eventType={}, retryCount={}",
                    event.getId(), event.getEventType(), event.getRetryCount(), ex);
            throw ex;
        }
    }

    private Object deserializePayload(OutboxEvent event) throws Exception {
        return switch (event.getEventType()) {
            case TRIP_CREATED -> objectMapper.readValue(event.getPayloadJson(), TripCreatedEvent.class);
            case TRIP_CANCELLED -> objectMapper.readValue(event.getPayloadJson(), TripCancelledEvent.class);
            case TRIP_DEPARTED -> objectMapper.readValue(event.getPayloadJson(), TripDepartedEvent.class);
            case TRIP_ARRIVED -> objectMapper.readValue(event.getPayloadJson(), TripArrivedEvent.class);
            default -> throw new IllegalArgumentException("Unknown event type: " + event.getEventType());
        };
    }

    private void handleFailedEvent(OutboxEvent event, Exception ex) {
        int newRetryCount = event.getRetryCount() + 1;
        outboxEventRepository.incrementRetry(event.getId());

        if (newRetryCount >= maxRetries) {
            log.error("Max retries exceeded for outbox event: id={}, eventType={}, retryCount={}",
                    event.getId(), event.getEventType(), newRetryCount);
            outboxEventRepository.markFailed(event.getId(), OutboxStatus.FAILED);
            sendToDlq(event);
        }
    }

    private void sendToDlq(OutboxEvent event) {
        try {
            String key = event.getAggregateId().toString();
            kafkaTemplate.send(dlqTopic, key, event.getPayloadJson());
            log.info("Sent failed event to DLQ: id={}, eventType={}, topic={}", event.getId(), event.getEventType(), dlqTopic);
        } catch (Exception ex) {
            log.error("Failed to send event to DLQ: id={}, eventType={}", event.getId(), event.getEventType(), ex);
        }
    }
}
