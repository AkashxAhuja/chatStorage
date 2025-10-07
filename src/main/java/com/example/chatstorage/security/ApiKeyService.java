package com.example.chatstorage.security;

import com.example.chatstorage.config.RateLimitProperties;
import com.example.chatstorage.config.SecurityProperties;
import com.example.chatstorage.dto.ApiKeyResponse;
import com.example.chatstorage.security.exception.ApiKeyExpiredException;
import com.example.chatstorage.security.exception.ApiKeyRateLimitExceededException;
import com.example.chatstorage.security.exception.InvalidApiKeyException;
import com.example.chatstorage.security.exception.MissingApiKeyException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ApiKeyService {

    private static final String API_KEY_HEADER = "X-API-KEY";

    private final String secret;
    private final long expirationMs;
    private final int requestLimit;
    private final Map<String, ApiKeyMetadata> activeKeys = new ConcurrentHashMap<>();

    public ApiKeyService(SecurityProperties securityProperties, RateLimitProperties rateLimitProperties) {
        if (securityProperties.getApiKeySecret() == null || securityProperties.getApiKeySecret().isBlank()) {
            throw new IllegalStateException("security.api-key-secret must be configured");
        }
        if (securityProperties.getKeyExpirationMs() <= 0) {
            throw new IllegalStateException("KEY_EXPIRATION_MS must be greater than zero");
        }
        if (rateLimitProperties.getRequestsPerMinute() <= 0) {
            throw new IllegalStateException("RATE_LIMIT_REQUESTS_PER_MINUTE must be greater than zero");
        }
        this.secret = securityProperties.getApiKeySecret();
        this.expirationMs = securityProperties.getKeyExpirationMs();
        this.requestLimit = rateLimitProperties.getRequestsPerMinute();
    }

    public ApiKeyResponse generateApiKey() {
        long issuedAt = System.currentTimeMillis();
        long expiresAt = issuedAt + expirationMs;
        String rawKey = issuedAt + ":" + secret;
        String encoded = Base64.getEncoder().encodeToString(rawKey.getBytes(StandardCharsets.UTF_8));
        ApiKeyMetadata metadata = new ApiKeyMetadata(issuedAt, expiresAt);
        activeKeys.put(encoded, metadata);
        return new ApiKeyResponse(encoded, Instant.ofEpochMilli(expiresAt));
    }

    public void validateAndRecordUsage(String encodedKey) {
        String sanitizedKey = (encodedKey != null) ? encodedKey.trim() : null;
        if (sanitizedKey == null || sanitizedKey.isEmpty()) {
            throw new MissingApiKeyException("Missing API key header '" + API_KEY_HEADER + "'. Generate a new key to proceed.", API_KEY_HEADER);
        }

        DecodedApiKey decoded = decodeApiKey(sanitizedKey);
        if (!secret.equals(decoded.secret())) {
            throw new InvalidApiKeyException("Invalid API key. Generate a new key to proceed.");
        }

        ApiKeyMetadata metadata = activeKeys.get(sanitizedKey);
        if (metadata == null || metadata.getIssuedAt() != decoded.issuedAt()) {
            throw new InvalidApiKeyException("Invalid API key. Generate a new key to proceed.");
        }

        long now = System.currentTimeMillis();
        synchronized (metadata) {
            if (now >= metadata.getExpiresAt()) {
                activeKeys.remove(sanitizedKey);
                throw new ApiKeyExpiredException("API key expired. Generate a new key to proceed.");
            }

            if (metadata.getRequestCount() >= requestLimit) {
                activeKeys.remove(sanitizedKey);
                throw new ApiKeyRateLimitExceededException("Rate limit exceeded. Generate a new key to proceed.");
            }

            metadata.incrementRequestCount();
        }
    }

    private DecodedApiKey decodeApiKey(String encodedKey) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(encodedKey);
            String decoded = new String(decodedBytes, StandardCharsets.UTF_8);
            String[] parts = decoded.split(":", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid format");
            }
            long issuedAt = Long.parseLong(parts[0]);
            return new DecodedApiKey(issuedAt, parts[1]);
        } catch (IllegalArgumentException ex) {
            throw new InvalidApiKeyException("Invalid API key. Generate a new key to proceed.");
        }
    }

    private record DecodedApiKey(long issuedAt, String secret) {
    }

    private static final class ApiKeyMetadata {
        private final long issuedAt;
        private final long expiresAt;
        private int requestCount;

        private ApiKeyMetadata(long issuedAt, long expiresAt) {
            this.issuedAt = issuedAt;
            this.expiresAt = expiresAt;
            this.requestCount = 0;
        }

        private long getIssuedAt() {
            return issuedAt;
        }

        private long getExpiresAt() {
            return expiresAt;
        }

        private int getRequestCount() {
            return requestCount;
        }

        private void incrementRequestCount() {
            requestCount++;
        }
    }
}
