package org.example.tripservice.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.example.tripservice.BaseIntegrationTest;
import org.example.tripservice.dto.request.RouteCreateRequest;
import org.example.tripservice.model.Route;
import org.example.tripservice.repository.RouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.test.context.support.WithMockUser;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class RouteControllerIntegrationTest extends BaseIntegrationTest {

    @LocalServerPort
    private Integer port;

    @Autowired
    private RouteRepository routeRepository;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        routeRepository.deleteAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateRoute() {
        RouteCreateRequest request = new RouteCreateRequest("Berlin", "Munich");

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/routes")
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("fromCity", is("Berlin"))
                .body("toCity", is("Munich"));
    }

    @Test
    void shouldGetRoutes() {
        Route route = new Route();
        route.setFromCity("Berlin");
        route.setToCity("Munich");
        routeRepository.save(route);

        given()
                .when()
                .get("/routes")
                .then()
                .statusCode(200)
                .body("content", hasSize(1))
                .body("content[0].fromCity", is("Berlin"));
    }
}

