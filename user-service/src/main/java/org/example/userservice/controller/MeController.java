package org.example.userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.userservice.controller.docs.MeControllerDocs;
import org.example.userservice.dto.request.UpdateUserRequest;
import org.example.userservice.dto.response.ProfileResponse;
import org.example.userservice.service.UserService;
import org.example.userservice.util.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users/me")
@RequiredArgsConstructor
@Slf4j
public class MeController implements MeControllerDocs {

    private final UserService userService;

    @Override
    @GetMapping
    public ResponseEntity<ProfileResponse> getMyProfile() {
        UUID userId = SecurityUtils.currentUserId();
        log.info("Fetching profile for current user {}", userId);

        ProfileResponse profile = userService.getMyProfile(userId);
        return ResponseEntity.ok(profile);
    }

    @Override
    @PutMapping
    public ResponseEntity<ProfileResponse> updateMyProfile(
            @Valid @RequestBody UpdateUserRequest request
    ) {
        UUID userId = SecurityUtils.currentUserId();
        log.info("Updating profile for current user {}", userId);

        ProfileResponse updatedProfile =
                userService.updateMyProfile(userId, request);

        return ResponseEntity.ok(updatedProfile);
    }

    @Override
    @DeleteMapping
    public ResponseEntity<Void> deleteMyProfile() {
        UUID userId = SecurityUtils.currentUserId();
        log.info("Deleting profile for current user {}", userId);

        userService.deleteMyProfile(userId);
        return ResponseEntity.noContent().build();
    }
}
