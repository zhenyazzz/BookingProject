package org.example.paymentservice.service;

import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.paymentservice.model.Payment;
import org.example.paymentservice.model.PaymentStatus;
import org.example.paymentservice.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.example.paymentservice.dto.CreatePaymentRequest;
import org.example.paymentservice.dto.PaymentListItemResponse;
import org.example.paymentservice.dto.PaymentStatusResponse;

import com.stripe.param.checkout.SessionCreateParams;
import org.example.kafka.event.PaymentSucceededEvent;
import org.example.kafka.event.PaymentFailedEvent;

import java.time.LocalDateTime;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @Value("${payment.success-url}")
    private String successUrl;

    @Value("${payment.cancel-url}")
    private String cancelUrl;

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;


    @Transactional
    public String createPayment(CreatePaymentRequest request) throws StripeException {
        Payment payment = new Payment();
        payment.setOrderId(request.orderId());
        payment.setAmount(request.amount());
        payment.setCurrency(request.currency() != null ? request.currency().toUpperCase() : "USD");
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());
        
        payment = paymentRepository.save(payment);
        
        try {
            SessionCreateParams params = SessionCreateParams.builder()
                    .addPaymentMethodType(com.stripe.param.checkout.SessionCreateParams.PaymentMethodType.CARD)
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl + "?orderId=" + request.orderId())
                    .setCancelUrl(cancelUrl + "?orderId=" + request.orderId())
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency(request.currency() != null ? request.currency() : "usd")
                                                    .setUnitAmount(request.amount().multiply(BigDecimal.valueOf(100)).longValue())
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName(request.description() != null ? request.description() : "Order #" + request.orderId())
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .putMetadata("orderId", request.orderId().toString())
                    .setPaymentIntentData(
                        SessionCreateParams.PaymentIntentData.builder()
                            .putMetadata("orderId", request.orderId().toString())
                            .build()
                    )                    
                    .build();

            Session session = Session.create(params);
            
            payment.setPaymentIntentId(session.getPaymentIntent());
            
            
            log.info("Payment session created for order {}", request.orderId());
            return session.getUrl();
            
        } catch (Exception e) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw e;
        }
    }

    @Transactional
    public void handleStripeWebhook(String payload, String signature) {

        Event event;

        try {
            event = Webhook.constructEvent(payload, signature, webhookSecret);
        } catch (Exception e) {
            log.warn("Invalid Stripe webhook signature", e);
            return;
        }

        if (!isSupportedEvent(event.getType())) {
            log.debug("Ignoring Stripe event type: {}", event.getType());
            return;
        }

        PaymentIntent intent = event
                .getDataObjectDeserializer()
                .getObject()
                .filter(obj -> obj instanceof PaymentIntent)
                .map(obj -> (PaymentIntent) obj)
                .orElse(null);

        if (intent == null) {
            log.warn("Stripe event without PaymentIntent");
            return;
        }

        Payment payment = paymentRepository
                .findByPaymentIntentId(intent.getId())
                .orElse(null);

        if (payment == null) {
            log.warn("Payment not found for intent {}", intent.getId());
            return;
        }

        switch (event.getType()) {
            case "payment_intent.succeeded" -> handlePaymentSuccess(payment);
            case "payment_intent.payment_failed" -> handlePaymentFailure(payment);
        }
    }

    private boolean isSupportedEvent(String eventType) {
        return "payment_intent.succeeded".equals(eventType)
            || "payment_intent.payment_failed".equals(eventType);
    }
    

    @Transactional
    public void handlePaymentSuccess(Payment payment) {

        if (payment.getStatus() == PaymentStatus.SUCCEEDED) {
            log.info("Payment {} already succeeded, skipping", payment.getId());
            return;
        }

        payment.setStatus(PaymentStatus.SUCCEEDED);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        PaymentSucceededEvent event = new PaymentSucceededEvent(
                UUID.randomUUID(),
                payment.getOrderId(),
                payment.getId(),
                Instant.now()
        );

        kafkaTemplate.send(
                "payment.succeeded",
                payment.getOrderId().toString(),
                event
        );

        log.info("Payment succeeded for order {}", payment.getOrderId());
    }


    @Transactional
    public void handlePaymentFailure(Payment payment) {

        if (payment.getStatus() == PaymentStatus.FAILED) {
            log.info("Payment {} already failed, skipping", payment.getId());
            return;
        }

        payment.setStatus(PaymentStatus.FAILED);
        payment.setCancelledAt(LocalDateTime.now());
        paymentRepository.save(payment);

        PaymentFailedEvent event = new PaymentFailedEvent(
                UUID.randomUUID(),
                payment.getOrderId(),
                "Stripe payment failed",
                Instant.now()
        );

        kafkaTemplate.send(
                "payment.failed",
                payment.getOrderId().toString(),
                event
        );

        log.info("Payment failed for order {}", payment.getOrderId());
    }

    public PaymentStatusResponse getPaymentByOrderId(UUID orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for order " + orderId));
        return new PaymentStatusResponse(payment.getId(), payment.getStatus());
    }

    public List<PaymentListItemResponse> getPaymentsByOrderIds(List<UUID> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return List.of();
        }
        return paymentRepository.findByOrderIdIn(orderIds).stream()
                .map(p -> new PaymentListItemResponse(
                        p.getId(),
                        p.getOrderId(),
                        p.getAmount(),
                        p.getCurrency(),
                        p.getStatus(),
                        p.getCreatedAt(),
                        p.getPaidAt()
                ))
                .collect(Collectors.toList());
    }
}

