package org.example.userservice.dto.response;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import org.example.events.enums.Role;
import org.example.events.enums.UserStatus;

@Schema(description = "User profile response")
public record UserResponse(

    @Schema(description = "User id (Keycloak subject)")
    UUID id,

    @Schema(description = "User email (from Keycloak)")
    String email,

    @Schema(description = "First name")
    String firstName,

    @Schema(description = "Last name")
    String lastName,

    @Schema(description = "Phone number")
    String phoneNumber,

    @Schema(description = "Profile creation timestamp")
    Instant createdAt,

    @Schema(description = "Profile last update timestamp")
    Instant updatedAt
) {}

