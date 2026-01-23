package org.example.paymentservice.dto;

import org.example.paymentservice.model.PaymentStatus;

public record PaymentStatusResponse(
    Long id,
    PaymentStatus status
) {
}
