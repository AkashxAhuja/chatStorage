package com.example.chatstorage.security;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

@Component
public class ApiKeyInterceptor implements HandlerInterceptor {

    private static final String API_KEY_HEADER = "X-API-KEY";
    private static final List<String> WHITELIST_PATTERNS = List.of(
            "/", "/error",
            "/health", "/actuator/**",
            "/swagger-ui.html", "/swagger-ui/**",
            "/v3/api-docs", "/v3/api-docs/**", "/v3/api-docs.yaml",
            "/api/v1/api-keys"
    );
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final ApiKeyService apiKeyService;

    public ApiKeyInterceptor(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        if (isWhitelisted(request.getRequestURI())) {
            return true;
        }
        String apiKey = request.getHeader(API_KEY_HEADER);
        apiKeyService.validateAndRecordUsage(apiKey);
        return true;
    }

    private boolean isWhitelisted(String path) {
        return WHITELIST_PATTERNS.stream().anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
    }
}
