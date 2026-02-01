package org.example.userservice.dto.response;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

public record ProfileResponse(
    @Schema(description = "User id (Keycloak subject)")
    UUID id,

    @Schema(description = "User email (from Keycloak)")
    String email,

    @Schema(description = "First name")
    String firstName,

    @Schema(description = "Last name")
    String lastName,

    @Schema(description = "Phone number")
    String phoneNumber
) {

}
