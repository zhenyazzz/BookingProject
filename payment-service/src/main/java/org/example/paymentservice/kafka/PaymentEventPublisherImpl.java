package org.example.paymentservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.kafka.event.PaymentFailedEvent;
import org.example.kafka.event.PaymentSucceededEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventPublisherImpl implements PaymentEventPublisher{

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${payment.kafka.dlq-topic:payment.dead-letter}")
    private String dlqTopic;

    @Override
    @Retryable(
        retryFor = { Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 200, multiplier = 2.0)
    )
    public void publishPaymentFailed(PaymentFailedEvent event) {
        send("payment.failed", event.paymentId().toString(), event);
    }

    @Override
    @Retryable(
        retryFor = { Exception.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 200, multiplier = 2.0)
    )
    public void publishPaymentSucceeded(PaymentSucceededEvent event) {
        send("payment.succeeded", event.paymentId().toString(), event);
    }

    @Recover
    public void recoverPaymentFailed(RuntimeException ex, PaymentFailedEvent event) {
        log.error("Publishing PaymentFailedEvent failed after retries, paymentId={}", event.paymentId(), ex);
        sendToDlq(event.paymentId().toString(), event);
    }

    @Recover
    public void recoverPaymentSucceeded(RuntimeException ex, PaymentSucceededEvent event) {
        log.error("Publishing PaymentSucceededEvent failed after retries, paymentId={}", event.paymentId(), ex);
        sendToDlq(event.paymentId().toString(), event);
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
