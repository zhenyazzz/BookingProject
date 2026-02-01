package org.example.paymentservice.dto.analytics;

import java.math.BigDecimal;
import java.util.Map;

public record PaymentStatsResponse(
    BigDecimal totalSucceededAmount,
    Long totalCount,
    Map<String, Long> countByStatus
) {
}
