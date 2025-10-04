package com.example.chatstorage.security;

import com.example.chatstorage.config.SecurityProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-KEY";
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final List<String> WHITELIST_PATTERNS = List.of(
            "/", "/error",
            "/health", "/actuator/**",
            "/swagger-ui.html", "/swagger-ui/**",
            "/v3/api-docs", "/v3/api-docs/**", "/v3/api-docs.yaml"
    );

    private final Set<String> allowedApiKeys;

    public ApiKeyAuthenticationFilter(SecurityProperties securityProperties) {
        if (CollectionUtils.isEmpty(securityProperties.getApiKeys())) {
            throw new IllegalStateException("At least one API key must be configured");
        }
        this.allowedApiKeys = securityProperties.getApiKeys().stream()
                .map(String::trim)
                .filter(key -> !key.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String apiKey = request.getHeader(API_KEY_HEADER);
        if (apiKey == null || !allowedApiKeys.contains(apiKey)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Invalid or missing API key\",\"errorCode\":\"UNAUTHORIZED\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (HttpMethod.OPTIONS.matches(request.getMethod()))
            return true;
        String path = request.getRequestURI(); // or request.getServletPath()
        return WHITELIST_PATTERNS.stream().anyMatch(p -> PATH_MATCHER.match(p, path));
    }
}
