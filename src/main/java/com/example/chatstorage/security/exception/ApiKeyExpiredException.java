package com.example.chatstorage.security.exception;

public class ApiKeyExpiredException extends RuntimeException {
    public ApiKeyExpiredException(String message) {
        super(message);
    }
}
