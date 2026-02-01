package org.example.paymentservice.dto;

import org.example.paymentservice.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentListItemResponse(
    UUID id,
    UUID orderId,
    BigDecimal amount,
    String currency,
    PaymentStatus status,
    LocalDateTime createdAt,
    LocalDateTime paidAt
) {
}
