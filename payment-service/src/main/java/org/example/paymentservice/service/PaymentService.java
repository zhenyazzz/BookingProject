package org.example.paymentservice.service;

import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.paymentservice.dto.OrderDto;
import org.example.paymentservice.dto.PaymentResponse;
import org.example.paymentservice.model.Payment;
import org.example.paymentservice.model.PaymentStatus;
import org.example.paymentservice.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    private final OrderServiceClient orderServiceClient;

    private final PaymentRepository paymentRepository;

    @Transactional
    public PaymentResponse createPayment(Long orderId) throws StripeException {
        OrderDto order = orderServiceClient.getOrder(orderId);
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(order.totalPrice().longValue()*100 + order.totalPrice().longValue()%100)
                .setCurrency("usd")
                .setDescription("Order #" + order.id())
                .putMetadata("orderId", orderId.toString())
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build()
                )
                .build();
        PaymentIntent stripeIntent = PaymentIntent.create(params);
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setPaymentIntentId(stripeIntent.getId());
        payment.setClientSecret(stripeIntent.getClientSecret());
        payment.setAmount(order.totalPrice());
        payment.setCurrency("USD");
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());
        paymentRepository.save(payment);
        log.info("Payment created");
        return toPaymentResponse(stripeIntent);
    }

    @Transactional
    public void handleStripeWebhook(String payload, String signature) {
        try {

            Event event = Webhook.constructEvent(payload, signature, webhookSecret);

            PaymentIntent intent = (PaymentIntent) event.getData().getObject();
            String paymentIntentId = intent.getId();

            Payment payment = paymentRepository.findByPaymentIntentId(paymentIntentId)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            switch (event.getType()) {
                case "payment_intent.succeeded":
                    handlePaymentSuccess(payment, intent);
                    break;

                case "payment_intent.payment_failed":
                    handlePaymentFailure(payment, intent);
                    break;
            }

        } catch (Exception e) {
            log.error("Webhook processing failed", e);
            throw new RuntimeException("Webhook error");
        }
    }

    @Transactional
    public void handlePaymentSuccess(Payment payment, PaymentIntent intent) {
        payment.setStatus(PaymentStatus.SUCCEEDED);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        try {
            orderServiceClient.confirmOrder(payment.getOrderId());
            log.info("Order {} confirmed successfully", payment.getOrderId());
        } catch (Exception e) {
            log.error("Failed to confirm order: {}", payment.getOrderId(), e);
        }
    }

    @Transactional
    public void handlePaymentFailure(Payment payment, PaymentIntent intent) {
        payment.setStatus(PaymentStatus.FAILED);
        payment.setCancelledAt(LocalDateTime.now());
        paymentRepository.save(payment);

        try {
            orderServiceClient.cancelOrder(payment.getOrderId());
            log.info("Order {} cancelled due to payment failure", payment.getOrderId());
        } catch (Exception e) {
            log.error("Failed to cancel order: {}", payment.getOrderId(), e);
        }
    }

    public Payment getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId).orElseThrow(() -> new RuntimeException("Payment not found"));
    }

    private PaymentResponse toPaymentResponse(PaymentIntent stripeIntent) {
        return new PaymentResponse(stripeIntent.getId(),
                stripeIntent.getClientSecret(),
                stripeIntent.getAmount(),
                stripeIntent.getCurrency(),
                stripeIntent.getMetadata().get("orderId")
        );
    }
}

