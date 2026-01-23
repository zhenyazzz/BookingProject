package org.example.userservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.userservice.controller.docs.UserControllerDocs;
import org.example.userservice.dto.response.UserResponse;
import org.example.userservice.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.UUID;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class UserController implements UserControllerDocs {

    private final UserService userService;

    @Override
    @GetMapping
    public ResponseEntity<Page<UserResponse>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        log.info("Admin requested users list: page={}, size={}, sort={}",
                page, size, sort);

        Page<UserResponse> users =
                userService.getUsersWithPagination(page, size, sort);

        return ResponseEntity.ok(users);
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        log.info("Admin requested user profile {}", id);

        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        log.info("Admin deleting user profile {}", id);

        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
