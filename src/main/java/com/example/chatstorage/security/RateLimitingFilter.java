package com.example.chatstorage.security;

import com.example.chatstorage.config.RateLimitProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimitingFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-KEY";

    private static final Set<String> WHITELISTED_PATHS = Set.of("/actuator/health", "/health");

    private final RateLimitProperties properties;
    private final Map<String, RateLimitWindow> cache = new ConcurrentHashMap<>();
    private final Duration windowDuration = Duration.ofMinutes(1);

    public RateLimitingFilter(RateLimitProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String key = request.getHeader(API_KEY_HEADER);
        String identifier = (key != null && !key.isBlank()) ? key : request.getRemoteAddr();
        RateLimitWindow window = cache.computeIfAbsent(identifier, this::newWindow);
        if (window.tryConsume()) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Rate limit exceeded\",\"errorCode\":\"RATE_LIMIT_EXCEEDED\"}");
        }
    }

    private RateLimitWindow newWindow(String key) {
        return new RateLimitWindow(properties.getRequestsPerMinute(), windowDuration);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return WHITELISTED_PATHS.contains(request.getRequestURI());
    }

    private static final class RateLimitWindow {
        private final int limit;
        private final long windowSizeMillis;
        private long windowStart;
        private int count;

        private RateLimitWindow(int limit, Duration windowDuration) {
            this.limit = limit;
            this.windowSizeMillis = windowDuration.toMillis();
            this.windowStart = System.currentTimeMillis();
            this.count = 0;
        }

        private synchronized boolean tryConsume() {
            if (limit <= 0) {
                return false;
            }

            long now = System.currentTimeMillis();
            if (now - windowStart >= windowSizeMillis) {
                windowStart = now;
                count = 0;
            }

            if (count < limit) {
                count++;
                return true;
            }

            return false;
        }
    }
}
