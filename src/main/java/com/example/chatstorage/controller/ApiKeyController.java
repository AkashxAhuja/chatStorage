package com.example.chatstorage.controller;

import com.example.chatstorage.dto.ApiKeyResponse;
import com.example.chatstorage.security.ApiKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "API Key Management")
@RestController
@RequestMapping("/api/v1/api-keys")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    public ApiKeyController(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @Operation(
            summary = "Generate a new API key",
            description = "Generates a short-lived API key that expires after the configured duration.",
            security = {}
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiKeyResponse generateApiKey() {
        return apiKeyService.generateApiKey();
    }
}
