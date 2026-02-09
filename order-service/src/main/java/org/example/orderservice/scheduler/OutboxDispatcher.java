package org.example.orderservice.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kafka.event.OrderCancelledEvent;
import org.example.kafka.event.OrderConfirmedEvent;
import org.example.kafka.event.OrderCreatedEvent;
import org.example.orderservice.kafka.OrderEventPublisher;
import org.example.orderservice.model.OutboxEvent;
import org.example.orderservice.model.OutboxStatus;
import org.example.orderservice.repository.OutboxEventRepository;
import org.springframework.beans.factory.annotation.Value;
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
    private final OrderEventPublisher orderEventPublisher;
    private final ObjectMapper objectMapper;

    @Value("${order.outbox.max-retries:5}")
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
                case ORDER_CREATED -> {
                    OrderCreatedEvent orderEvent = (OrderCreatedEvent) payload;
                    orderEventPublisher.publishOrderCreated(orderEvent);
                }
                case ORDER_CONFIRMED -> {
                    OrderConfirmedEvent orderEvent = (OrderConfirmedEvent) payload;
                    orderEventPublisher.publishOrderConfirmed(orderEvent);
                }
                case ORDER_CANCELLED -> {
                    OrderCancelledEvent orderEvent = (OrderCancelledEvent) payload;
                    orderEventPublisher.publishOrderCancelled(orderEvent);
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
            case ORDER_CREATED -> objectMapper.readValue(event.getPayloadJson(), OrderCreatedEvent.class);
            case ORDER_CONFIRMED -> objectMapper.readValue(event.getPayloadJson(), OrderConfirmedEvent.class);
            case ORDER_CANCELLED -> objectMapper.readValue(event.getPayloadJson(), OrderCancelledEvent.class);
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
        orderEventPublisher.sendToDlq(event.getAggregateId().toString(), event.getPayloadJson());
    }
}
