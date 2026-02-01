package org.example.bookingservice.dto.analytics;

import java.time.LocalDate;
import java.util.Map;

public record BookingStatsResponse(
    Long totalBookings,
    Map<String, Long> bookingsByStatus,
    Double conversionRate,
    Map<LocalDate, DailyBookingStats> dailyStats
) {
    public record DailyBookingStats(
        Long bookingCount,
        Long totalSeats
    ) {}
}
