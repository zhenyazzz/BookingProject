package org.example.tripservice.dto.analytics;

import java.util.UUID;

public record RouteStatsResponse(
    UUID routeId,
    String fromCity,
    String toCity,
    Long tripCount
) {}
