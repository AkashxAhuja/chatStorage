package com.example.chatstorage.dto;

import java.time.Instant;

public class ApiKeyResponse {

    private final String apiKey;
    private final Instant expiresAt;

    public ApiKeyResponse(String apiKey, Instant expiresAt) {
        this.apiKey = apiKey;
        this.expiresAt = expiresAt;
    }

    public String getApiKey() {
        return apiKey;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
