package org.example.bookingservice.service;

import lombok.RequiredArgsConstructor;
import org.example.bookingservice.dto.analytics.BookingStatsResponse;
import org.example.bookingservice.dto.analytics.PopularTripResponse;
import org.example.bookingservice.model.BookingStatus;
import org.example.bookingservice.repository.BookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingAnalyticsService {
    
    private final BookingRepository bookingRepository;
    
    @Transactional(readOnly = true)
    public BookingStatsResponse getBookingStats(LocalDate startDate, LocalDate endDate) {
        Instant startInstant = startDate != null ? startDate.atStartOfDay().toInstant(ZoneOffset.UTC) : null;
        Instant endInstant = endDate != null ? endDate.atTime(23, 59, 59).toInstant(ZoneOffset.UTC) : null;
        
        List<Object[]> statusCounts = bookingRepository.countBookingsByStatus(startInstant, endInstant);
        Map<String, Long> bookingsByStatus = statusCounts.stream()
            .collect(Collectors.toMap(
                row -> ((BookingStatus) row[0]).name(),
                row -> ((Number) row[1]).longValue()
            ));
        
        long totalBookings = bookingsByStatus.values().stream()
            .mapToLong(Long::longValue)
            .sum();
        
        Object[] conversionData = bookingRepository.calculateConversionRate(startInstant, endInstant);
        double conversionRate = 0.0;
        if (conversionData != null && totalBookings > 0) {
            long confirmed = ((Number) conversionData[0]).longValue();
            conversionRate = (confirmed * 100.0) / totalBookings;
        }
        
        List<Object[]> dailyStats = bookingRepository.getDailyBookingStats(startDate, endDate);
        Map<LocalDate, BookingStatsResponse.DailyBookingStats> dailyStatsMap = dailyStats.stream()
            .collect(Collectors.toMap(
                row -> (LocalDate) row[0],
                row -> new BookingStatsResponse.DailyBookingStats(
                    ((Number) row[1]).longValue(),
                    ((Number) row[2]).longValue()
                )
            ));
        
        return new BookingStatsResponse(
            totalBookings,
            bookingsByStatus,
            conversionRate,
            dailyStatsMap
        );
    }
    
    @Transactional(readOnly = true)
    public List<PopularTripResponse> getPopularTrips(LocalDate startDate, LocalDate endDate, int limit) {
        Instant startInstant = startDate != null ? startDate.atStartOfDay().toInstant(ZoneOffset.UTC) : null;
        Instant endInstant = endDate != null ? endDate.atTime(23, 59, 59).toInstant(ZoneOffset.UTC) : null;
        
        List<Object[]> popularTrips = bookingRepository.getPopularTrips(startInstant, endInstant);
        
        return popularTrips.stream()
            .limit(limit)
            .map(row -> new PopularTripResponse(
                (UUID) row[0],
                ((Number) row[1]).longValue(),
                ((Number) row[2]).longValue()
            ))
            .collect(Collectors.toList());
    }
}
