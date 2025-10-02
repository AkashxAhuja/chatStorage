package com.example.chatstorage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChatSessionCreateRequest {

    @NotBlank
    @Size(max = 10)
    private String userId;

    @NotBlank
    @Size(max = 20)
    private String title;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
