package org.example.bookingservice.controller;

import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.example.bookingservice.dto.request.BookingRequest;
import org.example.bookingservice.dto.response.BookingResponse;
import org.example.bookingservice.dto.response.CreateBookingResponse;
import org.example.bookingservice.model.BookingStatus;
import org.example.bookingservice.service.BookingService;
import org.example.bookingservice.controller.docs.BookingControllerDocs;
import org.example.bookingservice.util.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/booking")
@RequiredArgsConstructor
public class BookingController implements BookingControllerDocs {
    private final BookingService bookingService;

    @Override
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<CreateBookingResponse> createBooking(@Valid @RequestBody BookingRequest bookingRequest){
        return ResponseEntity.ok(bookingService.createBooking(bookingRequest));
    }

    @GetMapping(produces = "application/json", path = "/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<BookingResponse>> getMyBookings(
            @PageableDefault(size = 10) Pageable pageable
    ) {
        UUID currentUserId = SecurityUtils.currentUserId();
        return ResponseEntity.ok(bookingService.getAllBookings(currentUserId, null, null, pageable));
    }

    @Override
    @GetMapping(produces = "application/json", path = "/all")
    public ResponseEntity<Page<BookingResponse>> getAllBookings(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) UUID tripId,
            @RequestParam(required = false) BookingStatus status,
            @PageableDefault(size = 10) Pageable pageable
    ){
        return ResponseEntity.ok(bookingService.getAllBookings(userId, tripId, status, pageable));
    }

    @Override
    @DeleteMapping(produces = "application/json", path = "/cancel/{id}")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable UUID id){
        return ResponseEntity.ok(bookingService.cancelBooking(id));
    }
}
