package org.example.tripservice.dto.response;

import java.util.UUID;

public record RouteResponse(
    UUID id,
    String fromCity,
    String toCity
) {}

