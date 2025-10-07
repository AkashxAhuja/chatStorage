package com.example.chatstorage.security.exception;

public class MissingApiKeyException extends RuntimeException {

    private final String headerName;

    public MissingApiKeyException(String message, String headerName) {
        super(message);
        this.headerName = headerName;
    }

    public String getHeaderName() {
        return headerName;
    }
}
