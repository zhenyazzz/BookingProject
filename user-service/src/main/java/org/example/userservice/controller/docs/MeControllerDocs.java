package org.example.userservice.controller.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.userservice.dto.request.UpdateUserRequest;
import org.example.userservice.dto.response.ProfileResponse;
import org.springframework.http.ResponseEntity;

@Tag(name = "Me", description = "Operations for current authenticated user profile")
public interface MeControllerDocs {

    @Operation(
        summary = "Get my profile",
        description = "Returns profile of the currently authenticated user. Profile is created automatically if it does not exist.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Profile fetched successfully",
                content = @Content(schema = @Schema(implementation = ProfileResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
        }
    )
    ResponseEntity<ProfileResponse> getMyProfile();

    @Operation(
        summary = "Update my profile",
        description = "Updates profile data of the currently authenticated user",
        responses = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully",
                content = @Content(schema = @Schema(implementation = ProfileResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
        }
    )
    ResponseEntity<ProfileResponse> updateMyProfile(UpdateUserRequest request);

    @Operation(
        summary = "Delete my profile",
        description = "Deletes profile of the currently authenticated user",
        responses = {
            @ApiResponse(responseCode = "204", description = "Profile deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
        }
    )
    ResponseEntity<Void> deleteMyProfile();
}
