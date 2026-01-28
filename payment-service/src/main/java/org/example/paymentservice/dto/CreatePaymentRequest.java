package org.example.paymentservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePaymentRequest(
    UUID orderId,
    BigDecimal amount,
    String currency,
    String description
) {}
