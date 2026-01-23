package org.example.userservice.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.userservice.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

@Tag(name = "Users (Admin)", description = "Administrative operations on user profiles")
public interface UserControllerDocs {

    @Operation(
        summary = "Get users with pagination",
        description = "Returns a paginated list of user profiles",
        responses = {
            @ApiResponse(responseCode = "200", description = "List of users fetched successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied (ADMIN only)")
        }
    )
    ResponseEntity<Page<UserResponse>> getUsers(
            @Parameter(description = "Page number (0-based)") int page,
            @Parameter(description = "Number of items per page") int size,
            @Parameter(description = "Sorting, e.g. createdAt,desc") String sort
    );

    @Operation(
        summary = "Get user profile by id",
        description = "Returns profile data for a specific user",
        responses = {
            @ApiResponse(responseCode = "200", description = "User profile found",
                content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "404", description = "User profile not found"),
            @ApiResponse(responseCode = "403", description = "Access denied (ADMIN only)")
        }
    )
    ResponseEntity<UserResponse> getUserById(@Parameter(description = "User UUID") UUID id);

    @Operation(
        summary = "Delete user profile",
        description = "Permanently deletes a user profile from the system",
        responses = {
            @ApiResponse(responseCode = "204", description = "User profile deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User profile not found"),
            @ApiResponse(responseCode = "403", description = "Access denied (ADMIN only)")
        }
    )
    ResponseEntity<Void> deleteUser(@Parameter(description = "User UUID") UUID id);
}
