package org.example.tripservice.service;

import org.example.kafka.event.TripCreatedEvent;
import org.example.tripservice.dto.request.TripCreateRequest;
import org.example.tripservice.dto.response.RouteResponse;
import org.example.tripservice.dto.response.TripResponse;
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

import org.example.tripservice.model.TripStatus;
import org.example.kafka.event.BusType;
import org.example.kafka.event.EventType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private TripMapper tripMapper;

    @Mock
    private OutboxService outboxService;

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
                trip.getArrivalTime(), trip.getPrice(), trip.getTotalSeats(), 
                TripStatus.SCHEDULED, BusType.BUS_50);
    }

    @Test
    void createTrip_Success() {
        TripCreateRequest request = new TripCreateRequest(routeId, 
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(4),
                BigDecimal.valueOf(1500), BusType.BUS_50);
        TripCreatedEvent event = new TripCreatedEvent(UUID.randomUUID(), tripId, BusType.BUS_50, Instant.now());

        when(routeRepository.findById(routeId)).thenReturn(Optional.of(route));
        when(tripMapper.toEntity(eq(request), eq(route))).thenReturn(trip);
        when(tripRepository.save(any(Trip.class))).thenReturn(trip);
        when(tripMapper.toCreatedEvent(trip)).thenReturn(event);
        when(tripMapper.toResponse(trip)).thenReturn(tripResponse);

        TripResponse result = tripService.createTrip(request);

        assertNotNull(result);
        assertEquals(tripId, result.id());
        verify(tripRepository).save(any(Trip.class));
        verify(outboxService).saveEvent(tripId, EventType.TRIP_CREATED, event);
    }

    @Test
    void createTrip_InvalidTime() {
        TripCreateRequest request = new TripCreateRequest(routeId, 
                LocalDateTime.now().plusDays(1).plusHours(4), LocalDateTime.now().plusDays(1),
                BigDecimal.valueOf(1500), BusType.BUS_50);

        assertThrows(IllegalArgumentException.class, () -> tripService.createTrip(request));
        verifyNoInteractions(routeRepository, tripRepository, outboxService);
    }
}

