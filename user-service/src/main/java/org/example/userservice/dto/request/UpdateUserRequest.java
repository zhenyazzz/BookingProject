package org.example.userservice.dto.request;


import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request to update current user profile")
public record UpdateUserRequest(

    @Schema(description = "First name", example = "John")
    @Size(max = 100, message = "First name must be at most 100 characters")
    String firstName,

    @Schema(description = "Last name", example = "Doe")
    @Size(max = 100, message = "Last name must be at most 100 characters")
    String lastName,

    @Schema(description = "Phone number", example = "+375291234567")
    @Size(max = 20, message = "Phone number must be at most 20 characters")
    String phoneNumber
) {}

