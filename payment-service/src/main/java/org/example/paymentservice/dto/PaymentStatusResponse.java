package org.example.paymentservice.dto;

import org.example.paymentservice.model.PaymentStatus;
import java.util.UUID;

public record PaymentStatusResponse(
    UUID id,
    PaymentStatus status
) {
}
