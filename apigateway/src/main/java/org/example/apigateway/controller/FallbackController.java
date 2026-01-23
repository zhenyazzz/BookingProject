package org.example.apigateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping("/booking")
    public ResponseEntity<Map<String, Object>> bookingFallback() {
        return createFallbackResponse("booking-service", "Booking Service is temporarily unavailable");
    }

    @RequestMapping("/trip")
    public ResponseEntity<Map<String, Object>> tripFallback() {
        return createFallbackResponse("trip-service", "Trip Service is unavailable");
    }

    @RequestMapping("/inventory")
    public ResponseEntity<Map<String, Object>> inventoryFallback() {
        return createFallbackResponse("inventory-service", "Inventory information is currently unavailable");
    }

    @RequestMapping("/payment")
    public ResponseEntity<Map<String, Object>> paymentFallback() {
        return createFallbackResponse("payment-service", "Payment system is temporary offline");
    }

    private ResponseEntity<Map<String, Object>> createFallbackResponse(String serviceName, String message) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "service", serviceName,
                        "status", HttpStatus.SERVICE_UNAVAILABLE.value(),
                        "message", message,
                        "timestamp", Instant.now().toString()
                ));
    }
}
