package org.example.bookingservice.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import java.util.UUID;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static UUID currentUserId() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Unauthenticated access");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof Jwt jwt) {
            return UUID.fromString(jwt.getSubject());
        }

        throw new IllegalStateException(
            "Unsupported principal type: " + principal.getClass()
        );
    }

}

