package org.example.paymentservice.controller;

import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.paymentservice.dto.PaymentResponse;
import org.example.paymentservice.dto.PaymentStatusResponse;
import org.example.paymentservice.model.Payment;
import org.example.paymentservice.model.PaymentStatus;
import org.example.paymentservice.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@Slf4j
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-payment-intent/{orderId}")
    public ResponseEntity<PaymentResponse> createPaymentIntent(@PathVariable Long orderId) {
        try {
            PaymentResponse response = paymentService.createPayment(orderId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to create payment intent for order {}", orderId, e);
            return ResponseEntity.internalServerError()
                    .body(new PaymentResponse(null, "FAILED", 0L, "none", "0"));
        }
    }

    @GetMapping("/status/{orderId}")
    public ResponseEntity<PaymentStatusResponse> getStatus(@PathVariable Long orderId) {
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
