package com.example.chatstorage.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SecurityProperties {

    private final String apiKeySecret;
    private final long keyExpirationMs;

    public SecurityProperties(
            @Value("${SECURITY_API_KEY_SECRET:chatstorage}") String apiKeySecret,
            @Value("${KEY_EXPIRATION_MS:60000}") long keyExpirationMs) {
        this.apiKeySecret = apiKeySecret;
        this.keyExpirationMs = keyExpirationMs;
    }

    public String getApiKeySecret() {
        return apiKeySecret;
    }

    public long getKeyExpirationMs() {
        return keyExpirationMs;
    }
}
