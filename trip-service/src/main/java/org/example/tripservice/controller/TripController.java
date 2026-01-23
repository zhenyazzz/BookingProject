package org.example.tripservice.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import org.example.tripservice.service.TripService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.example.tripservice.dto.request.TripCreateRequest;
import org.example.tripservice.dto.request.TripUpdateRequest;
import org.example.tripservice.dto.response.TripResponse;
import java.util.UUID;

import org.example.tripservice.controller.docs.TripControllerDocs;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestParam;
import io.micrometer.observation.annotation.Observed;
import java.time.LocalDate;

@RestController
@RequestMapping("/trips")
@RequiredArgsConstructor
@Observed(name = "trip.controller")
public class TripController implements TripControllerDocs {
    private final TripService tripService;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<TripResponse> createTrip(@Valid @RequestBody TripCreateRequest request) {
        return ResponseEntity.ok(tripService.createTrip(request));
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<TripResponse> getTripById(@PathVariable UUID id) {
        return ResponseEntity.ok(tripService.getTripById(id));
    }

    @Override
    @GetMapping
    public ResponseEntity<Page<TripResponse>> getTrips(
            @RequestParam(required = false) String fromCity,
            @RequestParam(required = false) String toCity,
            @RequestParam(required = false) LocalDate date,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(tripService.getTrips(fromCity, toCity, date, pageable));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<TripResponse> updateTrip(@PathVariable UUID id, @Valid @RequestBody TripUpdateRequest request) {
        return ResponseEntity.ok(tripService.updateTrip(id, request));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTripById(@PathVariable UUID id) {
        tripService.deleteTripById(id);
        return ResponseEntity.noContent().build();
    }
}
