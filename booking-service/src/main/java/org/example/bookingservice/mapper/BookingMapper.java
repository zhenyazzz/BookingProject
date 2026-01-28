package org.example.bookingservice.mapper;

import org.example.bookingservice.model.Booking;
import org.example.bookingservice.dto.request.BookingRequest;
import org.example.bookingservice.dto.response.BookingResponse;
import org.example.bookingservice.dto.response.CreateBookingResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    BookingResponse toResponse(Booking booking);

    Booking toEntity(BookingRequest bookingRequest);

    default CreateBookingResponse toCreateResponse(
            Booking booking,
            UUID orderId,
            String paymentUrl,
            Instant reservationExpiresAt
    ) {
        return new CreateBookingResponse(
                booking.getId(),
                orderId,
                booking.getTripId(),
                booking.getSeatsCount(),
                booking.getStatus(),
                paymentUrl,
                reservationExpiresAt,
                booking.getCreatedAt()
        );
    }
}
