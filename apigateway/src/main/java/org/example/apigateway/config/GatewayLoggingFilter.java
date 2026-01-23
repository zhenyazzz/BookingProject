package org.example.apigateway.config;

import java.io.IOException;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GatewayLoggingFilter implements Filter {

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        long start = System.currentTimeMillis();

        String path = req.getRequestURI();
        String method = req.getMethod();
        String userId = resolveUser();

        try {
            chain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - start;

            log.info(
                "GATEWAY {} {} user={} status={} duration={}ms",
                method,
                path,
                userId,
                res.getStatus(),
                duration
            );
        }
    }

    private String resolveUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return "anonymous";
        }

        if (auth.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        }

        return auth.getName();
    }
}

