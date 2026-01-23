package org.example.tripservice.service;

import org.example.tripservice.dto.request.TripCreateRequest;
import org.example.tripservice.dto.request.TripUpdateRequest;
import org.example.tripservice.dto.response.RouteResponse;
import org.example.tripservice.dto.response.TripResponse;
import org.example.tripservice.exception.RouteNotFoundException;
import org.example.tripservice.exception.TripNotFoundException;
import org.example.tripservice.mapper.TripMapper;
import org.example.tripservice.model.Route;
import org.example.tripservice.model.Trip;
import org.example.tripservice.repository.RouteRepository;
import org.example.tripservice.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private TripMapper tripMapper;

    @InjectMocks
    private TripService tripService;

    private UUID tripId;
    private UUID routeId;
    private Route route;
    private Trip trip;
    private TripResponse tripResponse;

    @BeforeEach
    void setUp() {
        tripId = UUID.randomUUID();
        routeId = UUID.randomUUID();

        route = new Route();
        route.setId(routeId);
        route.setFromCity("Moscow");
        route.setToCity("Saint Petersburg");

        trip = new Trip();
        trip.setId(tripId);
        trip.setRoute(route);
        trip.setDepartureTime(LocalDateTime.now().plusDays(1));
        trip.setArrivalTime(LocalDateTime.now().plusDays(1).plusHours(4));
        trip.setPrice(BigDecimal.valueOf(1500));
        trip.setTotalSeats(50);

        RouteResponse routeResponse = new RouteResponse(routeId, "Moscow", "Saint Petersburg");
        tripResponse = new TripResponse(tripId, routeResponse, trip.getDepartureTime(), 
                trip.getArrivalTime(), trip.getPrice(), trip.getTotalSeats());
    }

    @Test
    void createTrip_Success() {
        TripCreateRequest request = new TripCreateRequest(routeId, 
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(4),
                BigDecimal.valueOf(1500), 50);

        when(routeRepository.findById(routeId)).thenReturn(Optional.of(route));
        when(tripMapper.toEntity(eq(request), eq(route))).thenReturn(trip);
        when(tripRepository.save(any(Trip.class))).thenReturn(trip);
        when(tripMapper.toResponse(trip)).thenReturn(tripResponse);

        TripResponse result = tripService.createTrip(request);

        assertNotNull(result);
        assertEquals(tripId, result.id());
        verify(tripRepository).save(any(Trip.class));
    }

    @Test
    void createTrip_InvalidTime() {
        TripCreateRequest request = new TripCreateRequest(routeId, 
                LocalDateTime.now().plusDays(1).plusHours(4), LocalDateTime.now().plusDays(1),
                BigDecimal.valueOf(1500), 50);

        assertThrows(IllegalArgumentException.class, () -> tripService.createTrip(request));
        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void createTrip_RouteNotFound() {
        TripCreateRequest request = new TripCreateRequest(routeId, 
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(4),
                BigDecimal.valueOf(1500), 50);

        when(routeRepository.findById(routeId)).thenReturn(Optional.empty());

        assertThrows(RouteNotFoundException.class, () -> tripService.createTrip(request));
    }

    @Test
    void getTripById_Success() {
        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(tripMapper.toResponse(trip)).thenReturn(tripResponse);

        TripResponse result = tripService.getTripById(tripId);

        assertNotNull(result);
        assertEquals(tripId, result.id());
    }

    @Test
    void getTripById_NotFound() {
        when(tripRepository.findById(tripId)).thenReturn(Optional.empty());

        assertThrows(TripNotFoundException.class, () -> tripService.getTripById(tripId));
    }

    @Test
    @SuppressWarnings("unchecked")
    void getTrips_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Trip> tripPage = new PageImpl<>(List.of(trip));

        when(tripRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(tripPage);
        when(tripMapper.toResponse(any(Trip.class))).thenReturn(tripResponse);

        Page<TripResponse> result = tripService.getTrips("Moscow", "Saint Petersburg", null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @SuppressWarnings("unchecked")
    void updateTrip_Success() {
        TripUpdateRequest request = new TripUpdateRequest(
                LocalDateTime.now().plusDays(2), 
                LocalDateTime.now().plusDays(2).plusHours(4),
                BigDecimal.valueOf(2000), 40);

        when(tripRepository.findById(tripId)).thenReturn(Optional.of(trip));
        when(tripRepository.save(trip)).thenReturn(trip);
        when(tripMapper.toResponse(trip)).thenReturn(tripResponse);

        TripResponse result = tripService.updateTrip(tripId, request);

        assertNotNull(result);
        verify(tripMapper).updateEntity(eq(request), eq(trip));
        verify(tripRepository).save(trip);
    }

    @Test
    void deleteTripById_Success() {
        when(tripRepository.existsById(tripId)).thenReturn(true);

        tripService.deleteTripById(tripId);

        verify(tripRepository).deleteById(tripId);
    }

    @Test
    void deleteTripById_NotFound() {
        when(tripRepository.existsById(tripId)).thenReturn(false);

        assertThrows(TripNotFoundException.class, () -> tripService.deleteTripById(tripId));
    }
}

