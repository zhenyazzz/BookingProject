package org.example.userservice.unit;

import jakarta.persistence.EntityManager;
import org.example.events.enums.Role;
import org.example.events.enums.RoleAction;
import org.example.events.enums.UserStatus;
import org.example.events.user.UserRegistrationEvent;
import org.example.events.user.UserRoleUpdateEvent;
import org.example.userservice.dto.request.CreateUserRequest;
import org.example.userservice.dto.request.UpdateUserRequest;
import org.example.userservice.dto.response.ProfileResponse;
import org.example.userservice.dto.response.UserResponse;
import org.example.userservice.exception.UserAlreadyExistsException;
import org.example.userservice.exception.UserNotFoundException;
import org.example.userservice.kafka.producer.KafkaProducerService;
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

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class UserServiceTests {

    @Mock
    private UserRepository userRepository;
    @Mock
    private KafkaProducerService kafkaProducerService;
    @Mock
    private UserMapper userMapper;
    @Mock
    private EntityManager entityManager;

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
        user.setUsername("testuser");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRoles(new java.util.HashSet<>(Set.of(Role.ROLE_USER)));
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());

        userResponse = new UserResponse(userId, "testuser", "test@example.com", "Test", "User", new java.util.HashSet<>(Set.of(Role.ROLE_USER)), UserStatus.ACTIVE, Instant.now(), Instant.now());
        profileResponse = new ProfileResponse(userId, "testuser", "test@example.com", "Test", "User", new java.util.HashSet<>(Set.of(Role.ROLE_USER)));
    }

    @Test
    void findUserById_UserFound_ReturnsUser() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User result = userService.findUserById(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void findUserById_UserNotFound_ThrowsUserNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.findUserById(userId));
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void getAllUsers_UsersExist_ReturnsUserResponseList() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        List<UserResponse> result = userService.getAllUsers();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(userResponse, result.get(0));
        verify(userRepository, times(1)).findAll();
        verify(userMapper, times(1)).toUserResponse(user);
    }

    @Test
    void getAllUsers_NoUsers_ReturnsEmptyList() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        List<UserResponse> result = userService.getAllUsers();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findAll();
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
    void createUser_NewUser_ReturnsUserResponseAndSendsEvent() {
        CreateUserRequest createUserRequest = new CreateUserRequest("newuser", "password", "newuser@example.com", "John", "Doe", Set.of(Role.ROLE_USER));
        when(userRepository.existsByEmail(createUserRequest.email())).thenReturn(false);
        when(userMapper.toUser(createUserRequest)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.createUser(createUserRequest);

        assertNotNull(result);
        assertEquals(userResponse, result);
        verify(userRepository, times(1)).existsByEmail(createUserRequest.email());
        verify(userMapper, times(1)).toUser(createUserRequest);
        verify(userRepository, times(1)).save(user);
        verify(kafkaProducerService, times(1)).sendUserCreatedEvent(any(UUID.class), any());
        verify(userMapper, times(1)).toUserResponse(user);
    }

    @Test
    void createUser_UserAlreadyExists_ThrowsUserAlreadyExistsException() {
        CreateUserRequest createUserRequest = new CreateUserRequest("existinguser", "password", "test@example.com", "Jane", "Doe", Set.of(Role.ROLE_USER));
        when(userRepository.existsByEmail(createUserRequest.email())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(createUserRequest));
        verify(userRepository, times(1)).existsByEmail(createUserRequest.email());
        verify(userMapper, never()).toUser(any(CreateUserRequest.class));
        verify(userRepository, never()).save(any());
        verify(kafkaProducerService, never()).sendUserCreatedEvent(any(UUID.class), any());
        verify(userMapper, never()).toUserResponse(any());
    }

    @Test
    void updateUser_UserFound_ReturnsUpdatedUserResponseAndSendsEvent() {
        UpdateUserRequest updateUserRequest = new UpdateUserRequest("updateduser", "password", "updated@example.com", "Jane", "Doe");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        doNothing().when(userMapper).updateUserFromRequest(updateUserRequest, user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.updateUser(userId, updateUserRequest);

        assertNotNull(result);
        assertEquals(userResponse, result);
        verify(userRepository, times(1)).findById(userId);
        verify(userMapper, times(1)).updateUserFromRequest(updateUserRequest, user);
        verify(userRepository, times(1)).save(user);
        verify(kafkaProducerService, times(1)).sendUserProfileUpdatedEvent(any(UUID.class), any());
        verify(userMapper, times(1)).toUserResponse(user);
    }

    @Test
    void updateUser_UserNotFound_ThrowsUserNotFoundException() {
        UpdateUserRequest updateUserRequest = new UpdateUserRequest("updateduser", "password", "updated@example.com", "Jane", "Doe");
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updateUser(userId, updateUserRequest));
        verify(userRepository, times(1)).findById(userId);
        verify(userMapper, never()).updateUserFromRequest(any(), any());
        verify(userRepository, never()).save(any());
        verify(kafkaProducerService, never()).sendUserProfileUpdatedEvent(any(UUID.class), any());
        verify(userMapper, never()).toUserResponse(any());
    }

    @Test
    void deleteUser_UserFound_DeletesUserAndSendsEvent() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).deleteById(userId);

        userService.deleteUser(userId);

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).deleteById(userId);
        verify(kafkaProducerService, times(1)).sendUserDeletedEvent(any(UUID.class), any());
    }

    @Test
    void deleteUser_UserNotFound_DoesNothing() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        userService.deleteUser(userId);

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).deleteById(any(UUID.class));
        verify(kafkaProducerService, never()).sendUserDeletedEvent(any(UUID.class), any());
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
    void getMyProfile_UserNotFound_ThrowsUserNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getMyProfile(userId));
        verify(userRepository, times(1)).findById(userId);
        verify(userMapper, never()).toProfileResponse(any());
    }

    @Test
    void updateMyProfile_UserFound_ReturnsUpdatedProfileResponseAndSendsEvent() {
        UpdateUserRequest updateUserRequest = new UpdateUserRequest("updatedprofile", "password", "updatedprofile@example.com", "John", "D");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        doNothing().when(userMapper).updateUserFromRequest(updateUserRequest, user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toProfileResponse(user)).thenReturn(profileResponse);

        ProfileResponse result = userService.updateMyProfile(userId, updateUserRequest);

        assertNotNull(result);
        assertEquals(profileResponse, result);
        verify(userRepository, times(1)).findById(userId);
        verify(userMapper, times(1)).updateUserFromRequest(updateUserRequest, user);
        verify(userRepository, times(1)).save(user);
        verify(kafkaProducerService, times(1)).sendUserProfileUpdatedEvent(any(UUID.class), any());
        verify(userMapper, times(1)).toProfileResponse(user);
    }

    @Test
    void updateMyProfile_UserNotFound_ThrowsUserNotFoundException() {
        UpdateUserRequest updateUserRequest = new UpdateUserRequest("updatedprofile", "password", "updatedprofile@example.com", "John", "D");
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updateMyProfile(userId, updateUserRequest));
        verify(userRepository, times(1)).findById(userId);
        verify(userMapper, never()).updateUserFromRequest(any(), any());
        verify(userRepository, never()).save(any());
        verify(kafkaProducerService, never()).sendUserProfileUpdatedEvent(any(UUID.class), any());
        verify(userMapper, never()).toProfileResponse(any());
    }

    @Test
    void deleteMyProfile_UserFound_DeletesUserAndSendsEvent() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(user);

        userService.deleteMyProfile(userId);

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).delete(user);
        verify(kafkaProducerService, times(1)).sendUserDeletedEvent(any(UUID.class), any());
    }

    @Test
    void deleteMyProfile_UserNotFound_ThrowsUserNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deleteMyProfile(userId));
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).delete(any(User.class));
        verify(kafkaProducerService, never()).sendUserDeletedEvent(any(UUID.class), any());
    }

    @Test
    void handleUserRegistration_NewUser_CreatesUser() {
        UserRegistrationEvent event = new UserRegistrationEvent(userId, "newuser", "new@example.com", "John", "Doe", new java.util.HashSet<>(Set.of(Role.ROLE_USER)));
        when(userRepository.existsById(event.id())).thenReturn(false);
        when(userMapper.toUser(event)).thenReturn(user);
        doNothing().when(userRepository).insertWithCustomId(any(UUID.class), anyString(), anyString(), anyString(), anyString(), anyString());
        doNothing().when(userRepository).insertUserRole(any(UUID.class), anyString());

        userService.handleUserRegistration(event);

        verify(userRepository, times(1)).existsById(event.id());
        verify(userMapper, times(1)).toUser(event);
        verify(userRepository, times(1)).insertWithCustomId(user.getId(), user.getUsername(), user.getEmail(), user.getFirstName(), user.getLastName(), UserStatus.ACTIVE.name());
        verify(userRepository, times(1)).insertUserRole(user.getId(), Role.ROLE_USER.name());
    }

    @Test
    void handleUserRegistration_UserAlreadyExists_ThrowsUserAlreadyExistsException() {
        UserRegistrationEvent event = new UserRegistrationEvent(userId, "existinguser", "test@example.com", "John", "Doe", new java.util.HashSet<>(Set.of(Role.ROLE_USER)));
        when(userRepository.existsById(event.id())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> userService.handleUserRegistration(event));
        verify(userRepository, times(1)).existsById(event.id());
        verify(userMapper, never()).toUser(any(UserRegistrationEvent.class));
        verify(userRepository, never()).insertWithCustomId(any(), any(), any(), any(), any(), any());
        verify(userRepository, never()).insertUserRole(any(), any());
    }

    @Test
    void handleUserRoleUpdate_AddRole_UpdatesUser() {
        UserRoleUpdateEvent event = new UserRoleUpdateEvent(userId, Role.ROLE_ADMIN, RoleAction.ADD);
        User userWithRoles = new User();
        userWithRoles.setId(userId);
        userWithRoles.setRoles(new java.util.HashSet<>(Set.of(Role.ROLE_USER)));
        when(userRepository.findById(userId)).thenReturn(Optional.of(userWithRoles));
        when(userRepository.save(userWithRoles)).thenReturn(userWithRoles);

        userService.handleUserRoleUpdate(event);

        assertTrue(userWithRoles.getRoles().contains(Role.ROLE_ADMIN));
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(userWithRoles);
    }

    @Test
    void handleUserRoleUpdate_RemoveRole_UpdatesUser() {
        UserRoleUpdateEvent event = new UserRoleUpdateEvent(userId, Role.ROLE_USER, RoleAction.REMOVE);
        User userWithRoles = new User();
        userWithRoles.setId(userId);
        userWithRoles.setRoles(new java.util.HashSet<>(Set.of(Role.ROLE_USER, Role.ROLE_ADMIN)));
        when(userRepository.findById(userId)).thenReturn(Optional.of(userWithRoles));
        when(userRepository.save(userWithRoles)).thenReturn(userWithRoles);

        userService.handleUserRoleUpdate(event);

        assertFalse(userWithRoles.getRoles().contains(Role.ROLE_USER));
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(userWithRoles);
    }

    @Test
    void handleUserRoleUpdate_UnsupportedAction_ThrowsIllegalArgumentException() {
        UserRoleUpdateEvent event = new UserRoleUpdateEvent(userId, Role.ROLE_USER, null);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(NullPointerException.class, () -> userService.handleUserRoleUpdate(event));
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any());
    }


}
