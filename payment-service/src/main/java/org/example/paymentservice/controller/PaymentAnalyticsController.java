package org.example.paymentservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.paymentservice.dto.analytics.PaymentStatsResponse;
import org.example.paymentservice.service.PaymentAnalyticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/payments/analytics")
@RequiredArgsConstructor
@Tag(name = "Payment Analytics", description = "Admin-only payment statistics")
public class PaymentAnalyticsController {

    private final PaymentAnalyticsService paymentAnalyticsService;

    @Operation(summary = "Get payment statistics", description = "Retrieves payment statistics for a period: total succeeded amount and counts by status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment stats retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied (Admin only)")
    })
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentStatsResponse> getPaymentStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(paymentAnalyticsService.getPaymentStats(startDate, endDate));
    }
}
