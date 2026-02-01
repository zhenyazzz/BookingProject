package org.example.orderservice.dto.analytics;

import java.util.Map;

public record OrderStatsResponse(
    Long totalOrders,
    Map<String, Long> ordersByStatus
) {}
