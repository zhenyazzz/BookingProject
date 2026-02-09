package org.example.tripservice.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.example.tripservice.dto.analytics.RouteStatsResponse;
import org.example.tripservice.service.RouteAnalyticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/routes/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Validated
public class RouteAnalyticsController {
    
    private final RouteAnalyticsService analyticsService;
    
    @GetMapping("/popular")
    public ResponseEntity<List<RouteStatsResponse>> getPopularRoutes(
        @RequestParam(required = false) 
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
        LocalDate startDate,
        
        @RequestParam(required = false) 
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
        LocalDate endDate,
        
        @RequestParam(defaultValue = "10") 
        @Min(value = 1, message = "Limit must be at least 1")
        @Max(value = 100, message = "Limit must not exceed 100")
        int limit
    ) {
        return ResponseEntity.ok(analyticsService.getRoutesWithTripCount(startDate, endDate, limit));
    }
}
