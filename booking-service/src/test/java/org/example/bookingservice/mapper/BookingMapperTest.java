package org.example.bookingservice.mapper;

import org.example.bookingservice.dto.response.CreateBookingResponse;
import org.example.bookingservice.model.BookingStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BookingMapperTest {

    @Test
    void createBookingResponse_holdsAllFields() {
        UUID bookingId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID tripId = UUID.randomUUID();
        Instant expiresAt = Instant.now().plusSeconds(900);
        Instant createdAt = Instant.now();
        String paymentUrl = "https://checkout.stripe.com/test";

        CreateBookingResponse response = new CreateBookingResponse(
                bookingId, orderId, tripId, 2, BookingStatus.WAITING_PAYMENT,
                paymentUrl, expiresAt, createdAt);

        assertNotNull(response);
        assertEquals(bookingId, response.bookingId());
        assertEquals(orderId, response.orderId());
        assertEquals(tripId, response.tripId());
        assertEquals(2, response.seatsCount());
        assertEquals(BookingStatus.WAITING_PAYMENT, response.status());
        assertEquals(paymentUrl, response.paymentUrl());
        assertEquals(expiresAt, response.reservationExpiresAt());
        assertEquals(createdAt, response.createdAt());
    }
}
