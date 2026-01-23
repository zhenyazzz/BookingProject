package org.example.tripservice.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.example.tripservice.repository.RouteRepository;
import org.example.tripservice.dto.request.RouteCreateRequest;
import org.example.tripservice.dto.request.RouteUpdateRequest;
import org.example.tripservice.dto.response.RouteResponse;
import org.example.tripservice.mapper.RouteMapper;
import org.example.tripservice.model.Route;
import org.example.tripservice.exception.RouteNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;
import jakarta.transaction.Transactional;
import io.micrometer.observation.annotation.Observed;

@Service
@RequiredArgsConstructor
@Slf4j
@Observed(name = "route.service")
public class RouteService {
    private final RouteRepository routeRepository;
    private final RouteMapper routeMapper;

    @Transactional
    public RouteResponse createRoute(RouteCreateRequest request) {
        if (routeRepository.existsByFromCityAndToCity(request.fromCity(), request.toCity())) {
            throw new IllegalArgumentException("Route already exists: " + request.fromCity() + " -> " + request.toCity());
        }
        Route route = routeRepository.save(routeMapper.toEntity(request));
        log.info("Route created: {} -> {}", route.getFromCity(), route.getToCity());
        return routeMapper.toResponse(route);
    }

    public RouteResponse getRouteById(UUID id) {
        log.info("Fetching route by id: {}", id);
        Route route = routeRepository.findById(id)
            .orElseThrow(() -> new RouteNotFoundException("Route not found with id: " + id));
        return routeMapper.toResponse(route);
    }

    public Page<RouteResponse> getRoutes(
        String fromCity,
        String toCity,
        Pageable pageable
    ) {
        log.info("Searching routes from: {} to: {}", fromCity, toCity);
        Page<Route> routes = routeRepository
            .findByFromCityContainingIgnoreCaseAndToCityContainingIgnoreCase(
                fromCity == null ? "" : fromCity,
                toCity == null ? "" : toCity,
                pageable
            );

        return routes.map(routeMapper::toResponse);
    }

    @Transactional
    public RouteResponse updateRoute(UUID id, RouteUpdateRequest request) {
        Route route = routeRepository.findById(id)
            .orElseThrow(() -> new RouteNotFoundException("Route not found with id: " + id));
        routeMapper.updateEntity(request, route);
        routeRepository.save(route);
        log.info("Route updated: {} -> {}", route.getFromCity(), route.getToCity());
        return routeMapper.toResponse(route);
    }

    @Transactional
    public void deleteRouteById(UUID id) {
        Route route = routeRepository.findById(id)
            .orElseThrow(() -> new RouteNotFoundException("Route not found with id: " + id));
        routeRepository.delete(route);
        log.info("Route deleted: {} -> {}", route.getFromCity(), route.getToCity());
    }

}
