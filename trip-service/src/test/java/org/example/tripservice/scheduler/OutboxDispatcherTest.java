package org.example.tripservice.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.kafka.event.BusType;
import org.example.kafka.event.EventType;
import org.example.kafka.event.TripCreatedEvent;
import org.example.tripservice.kafka.TripEventPublisher;
import org.example.tripservice.model.OutboxEvent;
import org.example.tripservice.model.OutboxStatus;
import org.example.tripservice.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxDispatcherTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private TripEventPublisher tripEventPublisher;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    private OutboxDispatcher outboxDispatcher;

    @BeforeEach
    void setUp() {
        outboxDispatcher = new OutboxDispatcher(outboxEventRepository, tripEventPublisher, kafkaTemplate, objectMapper);
        ReflectionTestUtils.setField(outboxDispatcher, "dlqTopic", "trip.dead-letter");
        ReflectionTestUtils.setField(outboxDispatcher, "maxRetries", 2);
    }

    @Test
    void processOutboxEvents_Success() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID aggregateId = UUID.randomUUID();
        String payloadJson = "{\"event\":\"created\"}";
        TripCreatedEvent payload = new TripCreatedEvent(UUID.randomUUID(), aggregateId, BusType.BUS_50, Instant.now());

        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setId(eventId);
        outboxEvent.setAggregateId(aggregateId);
        outboxEvent.setEventType(EventType.TRIP_CREATED);
        outboxEvent.setPayloadJson(payloadJson);
        outboxEvent.setRetryCount(0);

        when(outboxEventRepository.findTop100NewForUpdateSkipLocked()).thenReturn(List.of(outboxEvent));
        when(objectMapper.readValue(payloadJson, TripCreatedEvent.class)).thenReturn(payload);

        outboxDispatcher.processOutboxEvents();

        verify(tripEventPublisher).publishTripCreated(payload);
        verify(outboxEventRepository).markSent(eq(eventId), eq(OutboxStatus.SENT), any(LocalDateTime.class));
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
    }

    @Test
    void processOutboxEvents_FailureGoesToDlq() throws Exception {
        UUID eventId = UUID.randomUUID();
        UUID aggregateId = UUID.randomUUID();
        String payloadJson = "{\"event\":\"created\"}";
        TripCreatedEvent payload = new TripCreatedEvent(UUID.randomUUID(), aggregateId, BusType.BUS_50, Instant.now());

        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setId(eventId);
        outboxEvent.setAggregateId(aggregateId);
        outboxEvent.setEventType(EventType.TRIP_CREATED);
        outboxEvent.setPayloadJson(payloadJson);
        outboxEvent.setRetryCount(1);

        when(outboxEventRepository.findTop100NewForUpdateSkipLocked()).thenReturn(List.of(outboxEvent));
        when(objectMapper.readValue(payloadJson, TripCreatedEvent.class)).thenReturn(payload);
        doThrow(new RuntimeException("publish failed")).when(tripEventPublisher).publishTripCreated(payload);

        outboxDispatcher.processOutboxEvents();

        verify(outboxEventRepository).incrementRetry(eventId);
        verify(outboxEventRepository).markFailed(eventId, OutboxStatus.FAILED);
        verify(kafkaTemplate).send("trip.dead-letter", aggregateId.toString(), payloadJson);
        verify(outboxEventRepository, never()).markSent(any(), any(), any());
    }
}
