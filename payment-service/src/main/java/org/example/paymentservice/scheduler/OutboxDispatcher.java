package org.example.paymentservice.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.example.kafka.event.PaymentFailedEvent;
import org.example.kafka.event.PaymentSucceededEvent;
import org.example.paymentservice.kafka.PaymentEventPublisher;
import org.example.paymentservice.model.OutboxEvent;
import org.example.paymentservice.model.OutboxStatus;
import org.example.paymentservice.repository.OutboxEventRepository;
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
    private final PaymentEventPublisher paymentEventPublisher;
    private final ObjectMapper objectMapper;

    @Value("${payment.outbox.max-retries:5}")
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
                case PAYMENT_FAILED -> {
                    PaymentFailedEvent paymentEvent = (PaymentFailedEvent) payload;
                    paymentEventPublisher.publishPaymentFailed(paymentEvent);
                }
                case PAYMENT_SUCCEEDED -> {
                    PaymentSucceededEvent paymentEvent = (PaymentSucceededEvent) payload;
                    paymentEventPublisher.publishPaymentSucceeded(paymentEvent);
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
            case PAYMENT_FAILED -> objectMapper.readValue(event.getPayloadJson(), PaymentFailedEvent.class);
            case PAYMENT_SUCCEEDED -> objectMapper.readValue(event.getPayloadJson(), PaymentSucceededEvent.class);
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
        paymentEventPublisher.sendToDlq(event.getAggregateId().toString(), event.getPayloadJson());
    }

}
