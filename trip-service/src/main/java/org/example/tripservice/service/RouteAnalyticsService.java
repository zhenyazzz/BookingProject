package org.example.tripservice.service;

import lombok.RequiredArgsConstructor;
import org.example.tripservice.dto.analytics.RouteStatsResponse;
import org.example.tripservice.repository.RouteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RouteAnalyticsService {
    
    private final RouteRepository routeRepository;
    
    @Transactional(readOnly = true)
    public List<RouteStatsResponse> getRoutesWithTripCount(LocalDate startDate, LocalDate endDate, int limit) {
        List<Object[]> routes = routeRepository.getRoutesWithTripCount(startDate, endDate);
        
        return routes.stream()
            .limit(limit)
            .map(row -> new RouteStatsResponse(
                (UUID) row[0],
                (String) row[1],
                (String) row[2],
                ((Number) row[3]).longValue()
            ))
            .collect(Collectors.toList());
    }
}
