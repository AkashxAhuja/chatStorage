package com.example.chatstorage.error;

import com.example.chatstorage.security.exception.ApiKeyExpiredException;
import com.example.chatstorage.security.exception.ApiKeyRateLimitExceededException;
import com.example.chatstorage.security.exception.InvalidApiKeyException;
import com.example.chatstorage.security.exception.MissingApiKeyException;
import com.example.chatstorage.service.ResourceNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger errorLogger = LogManager.getLogger("ERROR_LOG");

    @ExceptionHandler(MissingApiKeyException.class)
    public ResponseEntity<ApiError> handleMissingApiKey(MissingApiKeyException ex, ServletWebRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "MISSING_API_KEY", ex.getMessage(), request, ex);
    }

    @ExceptionHandler(InvalidApiKeyException.class)
    public ResponseEntity<ApiError> handleInvalidApiKey(InvalidApiKeyException ex, ServletWebRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "INVALID_API_KEY", ex.getMessage(), request, ex);
    }

    @ExceptionHandler(ApiKeyExpiredException.class)
    public ResponseEntity<ApiError> handleExpiredApiKey(ApiKeyExpiredException ex, ServletWebRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "API_KEY_EXPIRED", ex.getMessage(), request, ex);
    }

    @ExceptionHandler(ApiKeyRateLimitExceededException.class)
    public ResponseEntity<ApiError> handleRateLimitExceeded(ApiKeyRateLimitExceededException ex, ServletWebRequest request) {
        return buildResponse(HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMIT_EXCEEDED", ex.getMessage(), request, ex);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex, ServletWebRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), request, ex);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, ServletWebRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining(", "));
        return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, request, ex);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex, ServletWebRequest request) {
        String message = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                .collect(Collectors.joining(", "));
        return buildResponse(HttpStatus.BAD_REQUEST, "CONSTRAINT_VIOLATION", message, request, ex);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleMessageNotReadable(HttpMessageNotReadableException ex, ServletWebRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_REQUEST_BODY", "Request body is malformed or missing", request, ex);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParameter(MissingServletRequestParameterException ex, ServletWebRequest request) {
        String message = "Missing required parameter '%s'".formatted(ex.getParameterName());
        return buildResponse(HttpStatus.BAD_REQUEST, "MISSING_PARAMETER", message, request, ex);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex, ServletWebRequest request) {
        String message = "Parameter '%s' has invalid value '%s'".formatted(ex.getName(), ex.getValue());
        return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER", message, request, ex);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex, ServletWebRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", ex.getMessage(), request, ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, ServletWebRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An unexpected error occurred", request, ex);
    }

    private ResponseEntity<ApiError> buildResponse(HttpStatus status, String code, String message, ServletWebRequest request, Exception ex) {
        ApiError error = new ApiError(OffsetDateTime.now(), code, message, request.getRequest().getRequestURI());
        logException(status, code, message, request, ex);
        return ResponseEntity.status(status).body(error);
    }

    private String formatFieldError(FieldError error) {
        return "%s %s".formatted(error.getField(), error.getDefaultMessage());
    }

    private void logException(HttpStatus status, String code, String message, ServletWebRequest request, Exception ex) {
        String path = request.getRequest().getRequestURI();
        if (status.is5xxServerError()) {
            errorLogger.error("status={} code={} path={} message={}", status.value(), code, path, message, ex);
        } else {
            errorLogger.warn("status={} code={} path={} message={} exception={}",
                    status.value(), code, path, message, ex.getMessage());
        }
    }
}
