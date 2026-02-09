package org.example.paymentservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.util.UUID;

public record CreatePaymentRequest(
    @NotNull UUID orderId,
    @NotNull
    @DecimalMin("0.01")
    @Digits(integer = 17, fraction = 2)
    BigDecimal amount,
    @NotNull @Pattern(regexp = "^[A-Za-z]{3}$") String currency,
    String description
) {}
