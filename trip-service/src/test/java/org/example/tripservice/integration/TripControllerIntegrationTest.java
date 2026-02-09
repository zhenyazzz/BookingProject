package org.example.tripservice.integration;

import org.example.kafka.event.BusType;
import org.example.tripservice.BaseIntegrationTest;
import org.example.tripservice.dto.response.TripResponse;
import org.example.tripservice.model.Route;
import org.example.tripservice.model.Trip;
import org.example.tripservice.model.TripStatus;
import org.example.tripservice.repository.RouteRepository;
import org.example.tripservice.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@Tag("integration")
class TripControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private RouteRepository routeRepository;

    private UUID tripId;

    @BeforeEach
    void setUp() {
        tripRepository.deleteAll();
        routeRepository.deleteAll();

        Route route = new Route();
        route.setFromCity("Moscow");
        route.setToCity("Saint Petersburg");
        route = routeRepository.saveAndFlush(route);

        Trip trip = new Trip();
        trip.setRoute(route);
        trip.setDepartureTime(LocalDateTime.now().plusDays(1));
        trip.setArrivalTime(LocalDateTime.now().plusDays(1).plusHours(4));
        trip.setPrice(BigDecimal.valueOf(1500));
        trip.setTotalSeats(50);
        trip.setStatus(TripStatus.SCHEDULED);
        trip.setBusType(BusType.BUS_50);
        tripId = tripRepository.saveAndFlush(trip).getId();
    }

    @Test
    void getTripById_Success() {
        TripResponse response = restTemplate.getForObject(
                "/trips/{id}",
                TripResponse.class,
                tripId
        );

        assertNotNull(response);
        assertEquals(tripId, response.id());
        assertEquals(TripStatus.SCHEDULED, response.status());
        assertNotNull(response.route());
        assertEquals("Moscow", response.route().fromCity());
    }

    @Test
    void getTripById_NotFound() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/trips/{id}",
                String.class,
                UUID.randomUUID()
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
