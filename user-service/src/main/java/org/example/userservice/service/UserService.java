package org.example.userservice.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.userservice.dto.request.UpdateUserRequest;
import org.example.userservice.dto.response.ProfileResponse;
import org.example.userservice.dto.response.UserResponse;
import org.example.userservice.exception.UserNotFoundException;
import org.example.userservice.mapper.UserMapper;
import org.example.userservice.model.User;
import org.example.userservice.repository.UserRepository;
import org.example.userservice.util.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public ProfileResponse getMyProfile(UUID userId) {
        log.info("Fetching profile for user {}", userId);

        User user = userRepository.findById(userId)
                .orElseGet(() -> createProfile(userId));

        String tokenPhone = SecurityUtils.currentUserPhoneNumber();
        if (user.getPhoneNumber() == null && tokenPhone != null) {
            user.setPhoneNumber(tokenPhone);
            userRepository.save(user);
        }

        return userMapper.toProfileResponse(user);
    }

    @Transactional
    public ProfileResponse updateMyProfile(UUID userId, UpdateUserRequest request) {
        User user = findExistingUser(userId);

        log.info("Updating profile for user {}", userId);
        UpdateUserRequest normalizedRequest = normalizeUpdateRequest(request);
        userMapper.updateUserFromRequest(normalizedRequest, user);

        return userMapper.toProfileResponse(user);
    }

    @Transactional
    public void deleteMyProfile(UUID userId) {
        User user = findExistingUser(userId);

        log.info("Deleting profile for user {}", userId);
        userRepository.delete(user);
    }

    @Transactional
    public Page<UserResponse> getUsersWithPagination(int page, int size, String sort) {
        List<String> allowedSortFields = List.of("createdAt", "email", "firstName", "lastName", "phoneNumber");
        if (!allowedSortFields.contains(sort)) {
            log.warn("Invalid sort parameter '{}', defaulting to createdAt DESC", sort);
            sort = "createdAt,desc";
        }
        Pageable pageable = PageRequest.of(page, size, parseSort(sort));

        log.info("Admin requested users list: page={}, size={}, sort={}", page, size, sort);
        return userRepository.findAll(pageable)
                .map(userMapper::toUserResponse);
    }

    @Transactional
    public UserResponse getUserById(UUID userId) {
        log.info("Admin requested user profile {}", userId);
        return userMapper.toUserResponse(findExistingUser(userId));
    }

    @Transactional
    public void deleteUser(UUID userId) {
        User user = findExistingUser(userId);

        log.info("Admin deleting user profile {}", userId);
        userRepository.delete(user);
    }

    private User findExistingUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException("User profile not found with id: " + userId));
    }

    @Transactional
    private User createProfile(UUID userId) {
        log.info("Creating profile for user {}", userId);

        User user = new User();
        user.setId(userId);

        String email = SecurityUtils.currentUserEmail();
        String firstName = SecurityUtils.currentUserFirstName();
        String lastName = SecurityUtils.currentUserLastName();
        String phoneNumber = SecurityUtils.currentUserPhoneNumber();
        if (email == null || email.isBlank()) {
            log.warn("Missing email claim for user {}", userId);
            throw new IllegalStateException("Email claim is missing in token");
        }
        
        user.setEmail(email);
        if (firstName != null) user.setFirstName(firstName);
        if (lastName != null) user.setLastName(lastName);
        if (phoneNumber != null) user.setPhoneNumber(phoneNumber);

        return userRepository.save(user);
    }

    private UpdateUserRequest normalizeUpdateRequest(UpdateUserRequest request) {
        return new UpdateUserRequest(
                normalizeField(request.firstName()),
                normalizeField(request.lastName()),
                normalizeField(request.phoneNumber())
        );
    }

    private String normalizeField(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private Sort parseSort(String sort) {
        try {
            String[] parts = sort.split(",");
            String property = parts[0];
            Sort.Direction direction =
                    parts.length > 1 && "desc".equalsIgnoreCase(parts[1])
                            ? Sort.Direction.DESC
                            : Sort.Direction.ASC;

            return Sort.by(direction, property);
        } catch (Exception e) {
            log.warn("Invalid sort parameter '{}', defaulting to createdAt DESC", sort);
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
    }
}

