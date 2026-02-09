package org.example.tripservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.example.kafka.event.EventType;
import org.example.tripservice.model.OutboxEvent;
import org.example.tripservice.model.OutboxStatus;
import org.example.tripservice.repository.OutboxEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void saveEvent(UUID aggregateId, EventType eventType, Object payload) {
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);

            OutboxEvent outboxEvent = new OutboxEvent();
            outboxEvent.setAggregateId(aggregateId);
            outboxEvent.setEventType(eventType);
            outboxEvent.setPayloadJson(payloadJson);
            outboxEvent.setStatus(OutboxStatus.NEW);
            outboxEvent.setRetryCount(0);

            outboxEventRepository.save(outboxEvent);
            log.info("Saved event to outbox: aggregateId={}, eventType={}", aggregateId, eventType);
        } catch (Exception ex) {
            log.error("Failed to save event to outbox: aggregateId={}, eventType={}", aggregateId, eventType, ex);
            throw new RuntimeException("Failed to save event to outbox", ex);
        }
    }
}
