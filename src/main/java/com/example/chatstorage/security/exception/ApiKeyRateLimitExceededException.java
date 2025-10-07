package com.example.chatstorage.security.exception;

public class ApiKeyRateLimitExceededException extends RuntimeException {
    public ApiKeyRateLimitExceededException(String message) {
        super(message);
    }
}
