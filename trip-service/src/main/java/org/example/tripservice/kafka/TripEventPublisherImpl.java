package org.example.tripservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kafka.event.TripArrivedEvent;
import org.example.kafka.event.TripCancelledEvent;
import org.example.kafka.event.TripCreatedEvent;
import org.example.kafka.event.TripDepartedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripEventPublisherImpl implements TripEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${trip.kafka.dlq-topic:trip.dead-letter}")
    private String dlqTopic;

    @Override
    @Retryable(
        value = { Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 200, multiplier = 2.0)
    )
    public void publishTripCreated(TripCreatedEvent event) {
        String key = event.tripId().toString();
        send(topicCreated(), key, event);
    }

    @Override
    @Retryable(
        value = { Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 200, multiplier = 2.0)
    )
    public void publishTripCancelled(TripCancelledEvent event) {
        String key = event.tripId().toString();
        send(topicCancelled(), key, event);
    }

    @Override
    @Retryable(
        value = { Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 200, multiplier = 2.0)
    )
    public void publishTripDeparted(TripDepartedEvent event) {
        String key = event.tripId().toString();
        send(topicDeparted(), key, event);
    }

    @Override
    @Retryable(
        value = { Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 200, multiplier = 2.0)
    )
    public void publishTripArrived(TripArrivedEvent event) {
        String key = event.tripId().toString();
        send(topicArrived(), key, event);
    }

    private void send(String topic, String key, Object payload) {
        try {
            var future = kafkaTemplate.send(topic, key, payload);
            var result = future.get(2, TimeUnit.SECONDS);

            if (result != null && result.getRecordMetadata() != null) {
                log.info(
                    "Kafka send ok, topic={}, key={}, partition={}, offset={}",
                    topic,
                    key,
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset()
                );
            } else {
                log.info("Kafka send ok, topic={}, key={}, no metadata", topic, key);
            }
        } catch (Exception ex) {
            log.warn("Kafka send failed, topic={}, key={}", topic, key, ex);
            throw new RuntimeException(ex);
        }
    }

    @Recover
    public void recoverTripCreated(RuntimeException ex, TripCreatedEvent event) {
        log.error("Publishing TripCreatedEvent failed after retries, tripId={}", event.tripId(), ex);
        sendToDlq(event.tripId().toString(), event);
    }

    @Recover
    public void recoverTripCancelled(RuntimeException ex, TripCancelledEvent event) {
        log.error("Publishing TripCancelledEvent failed after retries, tripId={}", event.tripId(), ex);
        sendToDlq(event.tripId().toString(), event);
    }

    @Recover
    public void recoverTripDeparted(RuntimeException ex, TripDepartedEvent event) {
        log.error("Publishing TripDepartedEvent failed after retries, tripId={}", event.tripId(), ex);
        sendToDlq(event.tripId().toString(), event);
    }

    @Recover
    public void recoverTripArrived(RuntimeException ex, TripArrivedEvent event) {
        log.error("Publishing TripArrivedEvent failed after retries, tripId={}", event.tripId(), ex);
        sendToDlq(event.tripId().toString(), event);
    }

    private void sendToDlq(String key, Object payload) {
        try {
            kafkaTemplate.send(dlqTopic, key, payload);
            log.info("Sent event to DLQ, topic={}, key={}", dlqTopic, key);
        } catch (Exception ex) {
            log.error("Failed to send event to DLQ, topic={}, key={}", dlqTopic, key, ex);
        }
    }

    private String topicCreated() {
        return "trip.created";
    }

    private String topicCancelled() {
        return "trip.cancelled";
    }

    private String topicDeparted() {
        return "trip.departed";
    }

    private String topicArrived() {
        return "trip.arrived";
    }
}

