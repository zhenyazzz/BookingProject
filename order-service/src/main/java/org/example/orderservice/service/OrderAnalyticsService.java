package org.example.orderservice.service;

import lombok.RequiredArgsConstructor;
import org.example.orderservice.dto.analytics.OrderStatsResponse;
import org.example.orderservice.dto.analytics.RevenueStatsResponse;
import org.example.orderservice.model.OrderStatus;
import org.example.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderAnalyticsService {
    
    private final OrderRepository orderRepository;
    
    @Transactional(readOnly = true)
    public RevenueStatsResponse getRevenueStats(LocalDate startDate, LocalDate endDate) {
        Instant startInstant = startDate != null ? startDate.atStartOfDay().toInstant(ZoneOffset.UTC) : null;
        Instant endInstant = endDate != null ? endDate.atTime(23, 59, 59).toInstant(ZoneOffset.UTC) : null;
        
        BigDecimal totalRevenue = orderRepository.calculateTotalRevenue(startInstant, endInstant);
        BigDecimal averageOrderValue = orderRepository.calculateAverageOrderValue(startInstant, endInstant);
        
        List<Object[]> dailyStats = orderRepository.getDailyRevenueStats(startDate, endDate);
        
        Map<LocalDate, RevenueStatsResponse.DailyRevenue> dailyRevenue = dailyStats.stream()
            .collect(Collectors.toMap(
                row -> (LocalDate) row[0],
                row -> new RevenueStatsResponse.DailyRevenue(
                    (BigDecimal) row[1],
                    ((Number) row[2]).longValue()
                )
            ));
        
        long orderCount = dailyRevenue.values().stream()
            .mapToLong(RevenueStatsResponse.DailyRevenue::orderCount)
            .sum();
        
        return new RevenueStatsResponse(
            totalRevenue,
            averageOrderValue,
            orderCount,
            dailyRevenue
        );
    }
    
    @Transactional(readOnly = true)
    public OrderStatsResponse getOrderStats(LocalDate startDate, LocalDate endDate) {
        Instant startInstant = startDate != null ? startDate.atStartOfDay().toInstant(ZoneOffset.UTC) : null;
        Instant endInstant = endDate != null ? endDate.atTime(23, 59, 59).toInstant(ZoneOffset.UTC) : null;
        
        List<Object[]> statusCounts = orderRepository.countOrdersByStatus(startInstant, endInstant);
        
        Map<String, Long> ordersByStatus = statusCounts.stream()
            .collect(Collectors.toMap(
                row -> ((OrderStatus) row[0]).name(),
                row -> ((Number) row[1]).longValue()
            ));
        
        long totalOrders = ordersByStatus.values().stream()
            .mapToLong(Long::longValue)
            .sum();
        
        return new OrderStatsResponse(totalOrders, ordersByStatus);
    }
}
