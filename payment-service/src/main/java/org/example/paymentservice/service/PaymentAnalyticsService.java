package org.example.paymentservice.service;

import lombok.RequiredArgsConstructor;
import org.example.paymentservice.dto.analytics.PaymentStatsResponse;
import org.example.paymentservice.model.PaymentStatus;
import org.example.paymentservice.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentAnalyticsService {

    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public PaymentStatsResponse getPaymentStats(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime end = endDate != null ? endDate.atTime(LocalTime.MAX) : null;

        BigDecimal totalSucceededAmount = paymentRepository.sumSucceededAmountByCreatedAtBetween(start, end);
        if (totalSucceededAmount == null) {
            totalSucceededAmount = BigDecimal.ZERO;
        }

        List<Object[]> countByStatusRows = paymentRepository.countByStatusAndCreatedAtBetween(start, end);
        Map<String, Long> countByStatus = countByStatusRows.stream()
                .collect(Collectors.toMap(
                        row -> ((PaymentStatus) row[0]).name(),
                        row -> ((Number) row[1]).longValue()
                ));

        long totalCount = countByStatus.values().stream().mapToLong(Long::longValue).sum();

        return new PaymentStatsResponse(totalSucceededAmount, totalCount, countByStatus);
    }
}
