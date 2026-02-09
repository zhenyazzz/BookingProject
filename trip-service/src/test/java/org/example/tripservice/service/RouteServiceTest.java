package org.example.tripservice.service;

import org.example.tripservice.dto.request.RouteCreateRequest;
import org.example.tripservice.dto.response.RouteResponse;
import org.example.tripservice.exception.RouteNotFoundException;
import org.example.tripservice.mapper.RouteMapper;
import org.example.tripservice.model.Route;
import org.example.tripservice.repository.RouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RouteServiceTest {

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private RouteMapper routeMapper;

    @InjectMocks
    private RouteService routeService;

    private UUID routeId;
    private Route route;
    private RouteResponse routeResponse;

    @BeforeEach
    void setUp() {
        routeId = UUID.randomUUID();
        route = new Route();
        route.setId(routeId);
        route.setFromCity("Moscow");
        route.setToCity("Saint Petersburg");

        routeResponse = new RouteResponse(routeId, "Moscow", "Saint Petersburg");
    }

    @Test
    void createRoute_Success() {
        RouteCreateRequest request = new RouteCreateRequest("Moscow", "Saint Petersburg");
        
        when(routeRepository.existsByFromCityAndToCity(request.fromCity(), request.toCity())).thenReturn(false);
        when(routeMapper.toEntity(request)).thenReturn(route);
        when(routeRepository.save(any(Route.class))).thenReturn(route);
        when(routeMapper.toResponse(route)).thenReturn(routeResponse);

        RouteResponse result = routeService.createRoute(request);

        assertNotNull(result);
        assertEquals(routeResponse.fromCity(), result.fromCity());
        verify(routeRepository).save(any(Route.class));
    }

    @Test
    void getRouteById_NotFound() {
        when(routeRepository.findById(routeId)).thenReturn(Optional.empty());

        assertThrows(RouteNotFoundException.class, () -> routeService.getRouteById(routeId));
    }
}

