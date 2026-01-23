package org.example.paymentservice.dto;

import java.math.BigDecimal;

public record OrderDto(
    Long id,
    BigDecimal totalPrice,
    Long  ticketCount,
    Long eventId
) {
}
