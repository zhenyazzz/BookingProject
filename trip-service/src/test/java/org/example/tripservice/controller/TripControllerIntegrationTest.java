package org.example.tripservice.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.example.tripservice.BaseIntegrationTest;
import org.example.tripservice.dto.request.TripCreateRequest;
import org.example.tripservice.model.Route;
import org.example.tripservice.repository.RouteRepository;
import org.example.tripservice.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class TripControllerIntegrationTest extends BaseIntegrationTest {

    @LocalServerPort
    private Integer port;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private TripRepository tripRepository;

    private Route savedRoute;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        tripRepository.deleteAll();
        routeRepository.deleteAll();

        Route route = new Route();
        route.setFromCity("Moscow");
        route.setToCity("Saint Petersburg");
        savedRoute = routeRepository.save(route);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateTrip() {
        TripCreateRequest request = new TripCreateRequest(
                savedRoute.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(4),
                BigDecimal.valueOf(1500.0),
                50
        );

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/trips")
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("price", is(1500.0f))
                .body("route.fromCity", is("Moscow"));
    }

    @Test
    void shouldGetTripById() {
        // First create a trip manually
        org.example.tripservice.model.Trip trip = new org.example.tripservice.model.Trip();
        trip.setRoute(savedRoute);
        trip.setDepartureTime(LocalDateTime.now().plusDays(1));
        trip.setArrivalTime(LocalDateTime.now().plusDays(1).plusHours(4));
        trip.setPrice(BigDecimal.valueOf(1200.0));
        trip.setTotalSeats(40);
        org.example.tripservice.model.Trip savedTrip = tripRepository.save(trip);

        given()
                .when()
                .get("/trips/" + savedTrip.getId())
                .then()
                .statusCode(200)
                .body("id", is(savedTrip.getId().toString()))
                .body("price", is(1200.0f));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenWhenUserCreatesTrip() {
        TripCreateRequest request = new TripCreateRequest(
                savedRoute.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(4),
                BigDecimal.valueOf(1500.0),
                50
        );

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/trips")
                .then()
                .statusCode(403);
    }
}

