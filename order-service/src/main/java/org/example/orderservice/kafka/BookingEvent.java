package org.example.orderservice.kafka;

import java.math.BigDecimal;

public record BookingEvent(
        Long userId,
        Long eventId,
        Long ticketCount,
        BigDecimal totalPrice
) {
}
