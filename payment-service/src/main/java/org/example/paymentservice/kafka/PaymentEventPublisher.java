package org.example.paymentservice.kafka;

import org.example.kafka.event.PaymentFailedEvent;
import org.example.kafka.event.PaymentSucceededEvent;

public interface PaymentEventPublisher {

    void publishPaymentFailed(PaymentFailedEvent event);

    void publishPaymentSucceeded(PaymentSucceededEvent event);

    void sendToDlq(String key, Object payload);

}
