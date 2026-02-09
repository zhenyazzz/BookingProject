package org.example.orderservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kafka.event.OrderCancelledEvent;
import org.example.kafka.event.OrderConfirmedEvent;
import org.example.kafka.event.OrderCreatedEvent;
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
public class OrderEventPublisherImpl implements OrderEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${order.kafka.dlq-topic:order.dead-letter}")
    private String dlqTopic;

    @Override
    @Retryable(
        retryFor = { Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 200, multiplier = 2.0)
    )
    public void publishOrderCreated(OrderCreatedEvent event) {
        send("order.created", event.orderId().toString(), event);
    }

    @Override
    @Retryable(
        retryFor = { Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 200, multiplier = 2.0)
    )
    public void publishOrderConfirmed(OrderConfirmedEvent event) {
        send("order.confirmed", event.orderId().toString(), event);
    }

    @Override
    @Retryable(
        retryFor = { Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 200, multiplier = 2.0)
    )
    public void publishOrderCancelled(OrderCancelledEvent event) {
        send("order.cancelled", event.orderId().toString(), event);
    }

    @Recover
    public void recoverOrderCreated(RuntimeException ex, OrderCreatedEvent event) {
        log.error("Publishing OrderCreatedEvent failed after retries, orderId={}", event.orderId(), ex);
        sendToDlq(event.orderId().toString(), event);
    }

    @Recover
    public void recoverOrderConfirmed(RuntimeException ex, OrderConfirmedEvent event) {
        log.error("Publishing OrderConfirmedEvent failed after retries, orderId={}", event.orderId(), ex);
        sendToDlq(event.orderId().toString(), event);
    }

    @Recover
    public void recoverOrderCancelled(RuntimeException ex, OrderCancelledEvent event) {
        log.error("Publishing OrderCancelledEvent failed after retries, orderId={}", event.orderId(), ex);
        sendToDlq(event.orderId().toString(), event);
    }

    @Override
    public void sendToDlq(String key, Object payload) {
        try {
            kafkaTemplate.send(dlqTopic, key, payload);
            log.info("Sent event to DLQ, topic={}, key={}", dlqTopic, key);
        } catch (Exception ex) {
            log.error("Failed to send event to DLQ, topic={}, key={}", dlqTopic, key, ex);
        }
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
}
