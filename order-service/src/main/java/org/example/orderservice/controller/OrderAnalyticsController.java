package org.example.orderservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.orderservice.controller.docs.OrderAnalyticsControllerDocs;
import org.example.orderservice.dto.analytics.OrderStatsResponse;
import org.example.orderservice.dto.analytics.RevenueStatsResponse;
import org.example.orderservice.service.OrderAnalyticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/orders/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class OrderAnalyticsController implements OrderAnalyticsControllerDocs {
    
    private final OrderAnalyticsService analyticsService;
    
    @Override
    @GetMapping("/revenue")
    public ResponseEntity<RevenueStatsResponse> getRevenue(
        @RequestParam(required = false) 
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
        LocalDate startDate,
        
        @RequestParam(required = false) 
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
        LocalDate endDate
    ) {
        return ResponseEntity.ok(analyticsService.getRevenueStats(startDate, endDate));
    }
    
    @Override
    @GetMapping("/stats")
    public ResponseEntity<OrderStatsResponse> getOrderStats(
        @RequestParam(required = false) 
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
        LocalDate startDate,
        
        @RequestParam(required = false) 
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
        LocalDate endDate
    ) {
        return ResponseEntity.ok(analyticsService.getOrderStats(startDate, endDate));
    }
}
