package org.example.orderservice.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.orderservice.dto.analytics.OrderStatsResponse;
import org.example.orderservice.dto.analytics.RevenueStatsResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Tag(name = "Order Analytics", description = "API for order statistics and revenue analytics")
public interface OrderAnalyticsControllerDocs {

    @Operation(summary = "Get revenue statistics", description = "Retrieves revenue statistics for a specified period.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Revenue stats retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied (Admin only)")
    })
    ResponseEntity<RevenueStatsResponse> getRevenue(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate
    );

    @Operation(summary = "Get order statistics", description = "Retrieves order statistics (counts by status) for a specified period.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order stats retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied (Admin only)")
    })
    ResponseEntity<OrderStatsResponse> getOrderStats(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate
    );
}
