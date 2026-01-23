package org.example.tripservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RouteCreateRequest(
    @NotBlank(message = "City of departure is required")
    @Size(max = 100, message = "City name cannot exceed 100 characters")
    String fromCity,

    @NotBlank(message = "City of arrival is required")
    @Size(max = 100, message = "City name cannot exceed 100 characters")
    String toCity
) {}

