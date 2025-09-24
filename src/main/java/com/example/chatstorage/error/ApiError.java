package com.example.chatstorage.error;

import java.time.OffsetDateTime;

public class ApiError {

    private OffsetDateTime timestamp;
    private String errorCode;
    private String message;
    private String path;

    public ApiError() {
    }

    public ApiError(OffsetDateTime timestamp, String errorCode, String message, String path) {
        this.timestamp = timestamp;
        this.errorCode = errorCode;
        this.message = message;
        this.path = path;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
