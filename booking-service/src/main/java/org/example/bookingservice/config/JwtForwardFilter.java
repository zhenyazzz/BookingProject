package org.example.bookingservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.bookingservice.util.BearerTokenHolder;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


public class JwtForwardFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION = "Authorization";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String auth = request.getHeader(AUTHORIZATION);
            if (auth != null && auth.startsWith("Bearer ")) {
                BearerTokenHolder.setToken(auth);
            }
            filterChain.doFilter(request, response);
        } finally {
            BearerTokenHolder.clear();
        }
    }
}
