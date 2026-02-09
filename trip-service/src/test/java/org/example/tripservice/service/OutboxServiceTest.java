package org.example.tripservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.example.kafka.event.EventType;
import org.example.tripservice.model.OutboxEvent;
import org.example.tripservice.model.OutboxStatus;
import org.example.tripservice.repository.OutboxEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxServiceTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OutboxService outboxService;

    @Test
    void saveEvent_Success() throws Exception {
        UUID aggregateId = UUID.randomUUID();
        EventType eventType = EventType.TRIP_CREATED;
        Object payload = new Object();
        String payloadJson = "{\"payload\":\"ok\"}";

        when(objectMapper.writeValueAsString(payload)).thenReturn(payloadJson);

        outboxService.saveEvent(aggregateId, eventType, payload);

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(captor.capture());

        OutboxEvent saved = captor.getValue();
        assertEquals(aggregateId, saved.getAggregateId());
        assertEquals(eventType, saved.getEventType());
        assertEquals(payloadJson, saved.getPayloadJson());
        assertEquals(OutboxStatus.NEW, saved.getStatus());
        assertEquals(0, saved.getRetryCount());
    }

    @Test
    void saveEvent_SerializationFailure() throws Exception {
        UUID aggregateId = UUID.randomUUID();
        EventType eventType = EventType.TRIP_CREATED;
        Object payload = new Object();

        when(objectMapper.writeValueAsString(payload)).thenThrow(new RuntimeException("boom"));

        assertThrows(RuntimeException.class, () -> outboxService.saveEvent(aggregateId, eventType, payload));
        verify(outboxEventRepository, never()).save(any(OutboxEvent.class));
    }
}
