package org.example.userservice.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.userservice.BaseIntegrationTest;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@Import(UserControllerIntegrationTest.TestJwtDecoderConfig.class)
class UserControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

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
    void getUsers_returnsPageWithUser() throws Exception {
        ResponseEntity<String> response = restTemplate.exchange(
                "/admin/users?page=0&size=20&sort=createdAt,desc",
                HttpMethod.GET,
                new HttpEntity<Void>(null, authHeaders("admin-token")),
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        JsonNode body = objectMapper.readTree(response.getBody());
        JsonNode content = body.get("content");
        assertNotNull(content);
        assertTrue(content.isArray());
        assertEquals(1, content.size());
        assertEquals("admin-check@example.com", content.get(0).get("email").asText());
    }

    @Test
    void getUserById_returnsUser() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/admin/users/{id}",
                HttpMethod.GET,
                new HttpEntity<Void>(null, authHeaders("admin-token")),
                String.class,
                userId
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        String body = response.getBody();
        assertNotNull(body);
        assertTrue(body.contains("admin-check@example.com"));
    }

    @Test
    void deleteUser_removesUser() {
        ResponseEntity<Void> response = restTemplate.exchange(
                "/admin/users/{id}",
                HttpMethod.DELETE,
                new HttpEntity<Void>(null, authHeaders("admin-token")),
                Void.class,
                userId
        );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertTrue(userRepository.findById(Objects.requireNonNull(userId)).isEmpty());
    }

    @TestConfiguration
    static class TestJwtDecoderConfig {
        @Bean
        @Primary
        JwtDecoder jwtDecoder() {
            return token -> {
                String safeToken = Objects.requireNonNull(token);
                return Jwt.withTokenValue(safeToken)
                    .header("alg", "none")
                    .claim("realm_access", Map.of("roles", rolesFor(safeToken)))
                    .subject("test-user")
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .build();
            };
        }
    }

    private static List<String> rolesFor(String token) {
        if (token.contains("admin")) {
            return List.of("ADMIN");
        }
        return List.of("USER");
    }

    private static HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(Objects.requireNonNull(token));
        return headers;
    }
}
