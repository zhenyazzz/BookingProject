package org.example.paymentservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.example.paymentservice.dto.CreatePaymentRequest;
import org.example.paymentservice.dto.PaymentListItemResponse;
import org.example.paymentservice.dto.PaymentResponse;
import org.example.paymentservice.dto.PaymentStatusResponse;
import org.example.paymentservice.model.Payment;
import org.example.paymentservice.service.PaymentService;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/payments")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Payment Controller", description = "API for managing payments via Stripe")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Create a new payment", description = "Creates a Stripe Checkout Session for the given order and returns the payment URL.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment created successfully, returns redirect URL"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
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

    @Operation(summary = "Get payments by order IDs", description = "Retrieves payments for the given order IDs. Used with GET /orders/me to show user payment history.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of payments retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PaymentListItemResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<PaymentListItemResponse>> getPaymentsByOrderIds(
            @RequestParam(name = "orderIds", required = false) List<UUID> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(paymentService.getPaymentsByOrderIds(orderIds));
    }

    @Operation(summary = "Get payment status", description = "Retrieves the current payment status for a specific order.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PaymentStatusResponse.class))),
            @ApiResponse(responseCode = "404", description = "Payment not found for the order")
    })
    @GetMapping("/status/{orderId}")
    public ResponseEntity<PaymentStatusResponse> getStatus(@PathVariable UUID orderId) {
        try {
            return ResponseEntity.ok(paymentService.getPaymentByOrderId(orderId));
        } catch (Exception e) {
            log.error("Failed to get status for order {} , {}", orderId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Stripe Webhook Handler", description = "Endpoint for receiving webhook events from Stripe (e.g., checkout.session.completed).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Webhook processed successfully"),
            @ApiResponse(responseCode = "500", description = "Error processing webhook")
    })
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
