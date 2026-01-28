package org.example.paymentservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.example.paymentservice.dto.CreatePaymentRequest;
import org.example.paymentservice.dto.PaymentResponse;
import org.example.paymentservice.dto.PaymentStatusResponse;
import org.example.paymentservice.model.Payment;
import org.example.paymentservice.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@Slf4j
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<String> createPayment(@RequestBody CreatePaymentRequest request) {
        try {
            String paymentUrl = paymentService.createPayment(request);
            return ResponseEntity.ok(paymentUrl);
        } catch (Exception e) {
            log.error("Failed to create payment for order {}", request.orderId(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/status/{orderId}")
    public ResponseEntity<PaymentStatusResponse> getStatus(@PathVariable UUID orderId) {
        try {
            Payment payment = paymentService.getPaymentByOrderId(orderId);
            return ResponseEntity.ok(new PaymentStatusResponse(payment.getId(), payment.getStatus()));
        } catch (Exception e) {
            log.error("Failed to get status for order {} , {}", orderId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {

        log.debug("Received Stripe webhook: {}", payload);

        try {
            paymentService.handleStripeWebhook(payload, signature);
            return ResponseEntity.ok("Webhook processed successfully");

        } catch (Exception e) {
            log.error("Webhook processing failed: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error processing webhook");
        }
    }
}
