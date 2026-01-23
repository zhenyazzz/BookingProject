package org.example.bookingservice.mapper;

import org.example.bookingservice.model.Booking;
import org.example.bookingservice.dto.request.BookingRequest;
import org.example.bookingservice.dto.response.BookingResponse;

public interface BookingMapper {

    BookingResponse toResponse(Booking booking);

    Booking toEntity(BookingRequest bookingRequest);


}
