package org.example.paymentservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RateLimitFilter extends OncePerRequestFilter {
    private static final String PAYMENTS_PATH = "/payments";
    private static final String STRIPE_WEBHOOK_PATH = "/payments/stripe";

    private final FixedWindowRateLimiter paymentsLimiter;
    private final FixedWindowRateLimiter webhookLimiter;

    public RateLimitFilter(
            @Value("${payment.rate-limit.payments-per-minute:60}") int paymentsPerMinute,
            @Value("${payment.rate-limit.webhook-per-minute:120}") int webhookPerMinute
    ) {
        this.paymentsLimiter = new FixedWindowRateLimiter(60_000, paymentsPerMinute);
        this.webhookLimiter = new FixedWindowRateLimiter(60_000, webhookPerMinute);
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (PAYMENTS_PATH.equals(path) || STRIPE_WEBHOOK_PATH.equals(path)) {
            String clientKey = resolveClientKey(request);
            FixedWindowRateLimiter limiter = STRIPE_WEBHOOK_PATH.equals(path) ? webhookLimiter : paymentsLimiter;
            if (!limiter.tryConsume(clientKey)) {
                long retryAfter = limiter.retryAfterSeconds(clientKey);
                response.setStatus(429);
                response.setHeader("Retry-After", String.valueOf(retryAfter));
                response.getWriter().write("Too Many Requests");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private String resolveClientKey(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static class FixedWindowRateLimiter {
        private final long windowMillis;
        private final int limit;
        private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

        private FixedWindowRateLimiter(long windowMillis, int limit) {
            this.windowMillis = windowMillis;
            this.limit = limit;
        }

        private boolean tryConsume(String key) {
            long now = System.currentTimeMillis();
            Window window = windows.computeIfAbsent(key, k -> new Window(now));
            synchronized (window) {
                if (now - window.startMillis >= windowMillis) {
                    window.startMillis = now;
                    window.count = 0;
                }
                if (window.count >= limit) {
                    return false;
                }
                window.count++;
                return true;
            }
        }

        private long retryAfterSeconds(String key) {
            Window window = windows.get(key);
            if (window == null) {
                return 1;
            }
            long now = System.currentTimeMillis();
            long remaining = windowMillis - (now - window.startMillis);
            if (remaining <= 0) {
                return 1;
            }
            return Math.max(1, remaining / 1000);
        }

        private static class Window {
            private long startMillis;
            private int count;

            private Window(long startMillis) {
                this.startMillis = startMillis;
                this.count = 0;
            }
        }
    }
}
