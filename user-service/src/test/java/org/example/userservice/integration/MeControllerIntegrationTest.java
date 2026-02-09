package org.example.userservice.integration;

import org.example.userservice.BaseIntegrationTest;
import org.example.userservice.dto.request.UpdateUserRequest;
import org.example.userservice.dto.response.ProfileResponse;
import org.example.userservice.model.User;
import org.example.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
@Import(MeControllerIntegrationTest.TestJwtDecoderConfig.class)
class MeControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        User user = new User();
        userId = UUID.randomUUID();
        user.setId(userId);
        user.setEmail("admin-check@example.com");
        user.setFirstName("Admin");
        user.setLastName("Check");
        user.setPhoneNumber("+375291234567");
        userRepository.saveAndFlush(user);
    }

    @Test
    void getMyProfile_returnsProfileResponse() {
        ResponseEntity<ProfileResponse> response = restTemplate.exchange(
                "/users/me",
                HttpMethod.GET,
                new HttpEntity<Void>(null, authHeaders(java.util.Objects.requireNonNull(userId).toString())),
                ProfileResponse.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ProfileResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("admin-check@example.com", body.email());
    }

    @Test
    void updateMyProfile_returnsProfileResponse() {
        UpdateUserRequest request = new UpdateUserRequest("John", "Doe", "+375291234567");
        ResponseEntity<ProfileResponse> response = restTemplate.exchange(
                "/users/me",
                HttpMethod.PUT,
                new HttpEntity<>(request, authHeaders(java.util.Objects.requireNonNull(userId).toString())),
                ProfileResponse.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ProfileResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("John", body.firstName());
        assertEquals("Doe", body.lastName());
        assertEquals("+375291234567", body.phoneNumber());
    }

    @TestConfiguration
    static class TestJwtDecoderConfig {
        @Bean
        @Primary
        JwtDecoder jwtDecoder() {
            return token -> {
                String safeToken = java.util.Objects.requireNonNull(token);
                return Jwt.withTokenValue(safeToken)
                        .header("alg", "none")
                        .claim("email", "admin-check@example.com")
                        .subject(safeToken)
                        .issuedAt(Instant.now())
                        .expiresAt(Instant.now().plusSeconds(3600))
                        .build();
            };
        }
    }

    private static HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(java.util.Objects.requireNonNull(token));
        return headers;
    }
}
