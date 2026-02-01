package org.example.userservice.unit;

import org.example.userservice.dto.request.UpdateUserRequest;
import org.example.userservice.dto.response.ProfileResponse;
import org.example.userservice.dto.response.UserResponse;
import org.example.userservice.exception.UserNotFoundException;
import org.example.userservice.mapper.UserMapper;
import org.example.userservice.model.User;
import org.example.userservice.repository.UserRepository;
import org.example.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTests {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private UUID userId;
    private User user;
    private UserResponse userResponse;
    private ProfileResponse profileResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPhoneNumber("+375291234567");
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());

        userResponse = new UserResponse(userId, "test@example.com", "Test", "User", "+375291234567", Instant.now(), Instant.now());
        profileResponse = new ProfileResponse(userId, "test@example.com", "Test", "User", "+375291234567");
    }

    @Test
    void getMyProfile_UserFound_ReturnsProfileResponse() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toProfileResponse(user)).thenReturn(profileResponse);

        ProfileResponse result = userService.getMyProfile(userId);

        assertNotNull(result);
        assertEquals(profileResponse, result);
        verify(userRepository, times(1)).findById(userId);
        verify(userMapper, times(1)).toProfileResponse(user);
    }

    @Test
    void updateMyProfile_UserFound_ReturnsUpdatedProfileResponse() {
        UpdateUserRequest updateUserRequest = new UpdateUserRequest("Jane", "Doe", "+375299999999");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        doNothing().when(userMapper).updateUserFromRequest(updateUserRequest, user);
        when(userMapper.toProfileResponse(user)).thenReturn(profileResponse);

        ProfileResponse result = userService.updateMyProfile(userId, updateUserRequest);

        assertNotNull(result);
        assertEquals(profileResponse, result);
        verify(userRepository, times(1)).findById(userId);
        verify(userMapper, times(1)).updateUserFromRequest(updateUserRequest, user);
        verify(userMapper, times(1)).toProfileResponse(user);
    }

    @Test
    void updateMyProfile_UserNotFound_ThrowsUserNotFoundException() {
        UpdateUserRequest updateUserRequest = new UpdateUserRequest("Jane", "Doe", "+375299999999");
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updateMyProfile(userId, updateUserRequest));
        verify(userRepository, times(1)).findById(userId);
        verify(userMapper, never()).updateUserFromRequest(any(), any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteMyProfile_UserFound_DeletesUser() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(user);

        userService.deleteMyProfile(userId);

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).delete(user);
    }

    @Test
    void deleteMyProfile_UserNotFound_ThrowsUserNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deleteMyProfile(userId));
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void getUsersWithPagination_UsersExist_ReturnsPage() {
        when(userRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(user)));
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        Page<UserResponse> result = userService.getUsersWithPagination(0, 10, "createdAt");

        assertNotNull(result);
        assertFalse(result.getContent().isEmpty());
        assertEquals(1, result.getContent().size());
        assertEquals(userResponse, result.getContent().get(0));
        verify(userRepository, times(1)).findAll(any(Pageable.class));
        verify(userMapper, times(1)).toUserResponse(user);
    }

    @Test
    void getUsersWithPagination_NoUsers_ReturnsEmptyPage() {
        when(userRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(Collections.emptyList()));

        Page<UserResponse> result = userService.getUsersWithPagination(0, 10, "createdAt");

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        verify(userRepository, times(1)).findAll(any(Pageable.class));
        verify(userMapper, never()).toUserResponse(any(User.class));
    }

    @Test
    void getUserById_UserFound_ReturnsUserResponse() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.getUserById(userId);

        assertNotNull(result);
        assertEquals(userResponse, result);
        verify(userRepository, times(1)).findById(userId);
        verify(userMapper, times(1)).toUserResponse(user);
    }

    @Test
    void getUserById_UserNotFound_ThrowsUserNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(userId));
        verify(userRepository, times(1)).findById(userId);
        verify(userMapper, never()).toUserResponse(any(User.class));
    }

    @Test
    void deleteUser_UserFound_DeletesUser() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(user);

        userService.deleteUser(userId);

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).delete(user);
    }

    @Test
    void deleteUser_UserNotFound_ThrowsUserNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(userId));
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).delete(any(User.class));
    }
}
