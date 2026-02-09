package org.example.tripservice.integration;

import org.example.tripservice.BaseIntegrationTest;
import org.example.tripservice.dto.response.RouteResponse;
import org.example.tripservice.model.Route;
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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@Tag("integration")
class RouteControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private TripRepository tripRepository;

    private UUID routeId;

    @BeforeEach
    void setUp() {
        tripRepository.deleteAll();
        routeRepository.deleteAll();

        Route route = new Route();
        route.setFromCity("Moscow");
        route.setToCity("Saint Petersburg");
        routeId = routeRepository.saveAndFlush(route).getId();
    }

    @Test
    void getRouteById_Success() {
        RouteResponse response = restTemplate.getForObject(
                "/routes/{id}",
                RouteResponse.class,
                routeId
        );

        assertNotNull(response);
        assertEquals(routeId, response.id());
        assertEquals("Moscow", response.fromCity());
        assertEquals("Saint Petersburg", response.toCity());
    }

    @Test
    void getRouteById_NotFound() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/routes/{id}",
                String.class,
                UUID.randomUUID()
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
