package org.example.tripservice.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

import org.example.tripservice.service.RouteService;
import jakarta.validation.Valid;

import java.util.UUID;

import org.example.tripservice.dto.request.RouteCreateRequest;
import org.example.tripservice.dto.response.RouteResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.example.tripservice.dto.request.RouteUpdateRequest;
import org.example.tripservice.controller.docs.RouteControllerDocs;
import org.springframework.security.access.prepost.PreAuthorize;
import io.micrometer.observation.annotation.Observed;

@RestController
@RequestMapping("/routes")
@RequiredArgsConstructor
@Observed(name = "route.controller")
public class RouteController implements RouteControllerDocs {
    private final RouteService routeService;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<RouteResponse> createRoute(@Valid @RequestBody RouteCreateRequest request) {
        return ResponseEntity.ok(routeService.createRoute(request));
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<RouteResponse> getRouteById(@PathVariable UUID id) {
        return ResponseEntity.ok(routeService.getRouteById(id));
    }

    @Override
    @GetMapping
    public ResponseEntity<Page<RouteResponse>> getRoutes(
            @RequestParam(required = false) String fromCity,
            @RequestParam(required = false) String toCity,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(
            routeService.getRoutes(fromCity, toCity, pageable)
        );
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<RouteResponse> updateRoute(@PathVariable UUID id, @Valid @RequestBody RouteUpdateRequest request) {
        return ResponseEntity.ok(routeService.updateRoute(id, request));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRouteById(@PathVariable UUID id) {
        routeService.deleteRouteById(id);
        return ResponseEntity.noContent().build();
    }
}
