package org.example.bookingservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.bookingservice.dto.analytics.BookingStatsResponse;
import org.example.bookingservice.dto.analytics.PopularTripResponse;
import org.example.bookingservice.service.BookingAnalyticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/booking/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class BookingAnalyticsController {
    
    private final BookingAnalyticsService analyticsService;

    @GetMapping("/stats")
    public ResponseEntity<BookingStatsResponse> getBookingStats(
        @RequestParam(required = false) 
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
        LocalDate startDate,
        
        @RequestParam(required = false) 
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
        LocalDate endDate
    ) {
        return ResponseEntity.ok(analyticsService.getBookingStats(startDate, endDate));
    }
    
    @GetMapping("/popular-trips")
    public ResponseEntity<List<PopularTripResponse>> getPopularTrips(
        @RequestParam(required = false) 
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
        LocalDate startDate,
        
        @RequestParam(required = false) 
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
        LocalDate endDate,
        
        @RequestParam(defaultValue = "10") 
        int limit
    ) {
        return ResponseEntity.ok(analyticsService.getPopularTrips(startDate, endDate, limit));
    }
}
