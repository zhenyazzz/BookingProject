package org.example.userservice.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.userservice.dto.request.CreateUserRequest;
import org.example.userservice.dto.request.UpdateUserRequest;
import org.example.userservice.dto.response.ProfileResponse;
import org.example.userservice.dto.response.UserResponse;
import org.example.userservice.exception.UserAlreadyExistsException;
import org.example.userservice.exception.UserNotFoundException;
import org.example.userservice.mapper.UserMapper;
import org.example.userservice.model.User;
import org.example.userservice.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public UserResponse getMyProfile(UUID userId) {
        log.info("Fetching profile for user {}", userId);

        User user = userRepository.findById(userId)
                .orElseGet(() -> createProfile(userId));

        return userMapper.toUserResponse(user);
    }

    @Transactional
    public UserResponse updateMyProfile(UUID userId, UpdateUserRequest request) {
        User user = findExistingUser(userId);

        log.info("Updating profile for user {}", userId);
        userMapper.updateUserFromRequest(request, user);

        return userMapper.toUserResponse(user);
    }

    @Transactional
    public void deleteMyProfile(UUID userId) {
        User user = findExistingUser(userId);

        log.info("Deleting profile for user {}", userId);
        userRepository.delete(user);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getUsersWithPagination(int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page, size, parseSort(sort));

        log.info("Admin requested users list: page={}, size={}, sort={}", page, size, sort);
        return userRepository.findAll(pageable)
                .map(userMapper::toUserResponse);
    }

    @Transactional(readOnly = true)
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

    private User createProfile(UUID userId) {
        log.info("Creating profile for user {}", userId);

        User user = new User();
        user.setId(userId);
        user.setEmail(SecurityUtils.currentUserEmail());

        return userRepository.save(user);
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

