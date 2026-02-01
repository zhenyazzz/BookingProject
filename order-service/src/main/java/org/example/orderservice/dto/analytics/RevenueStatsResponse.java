package org.example.orderservice.dto.analytics;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public record RevenueStatsResponse(
    BigDecimal totalRevenue,
    BigDecimal averageOrderValue,
    Long orderCount,
    Map<LocalDate, DailyRevenue> dailyRevenue
) {
    public record DailyRevenue(
        BigDecimal revenue,
        Long orderCount
    ) {}
}
