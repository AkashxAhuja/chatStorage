package com.example.chatstorage.dto;

import com.example.chatstorage.domain.SenderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ChatMessageCreateRequest {

    @NotNull
    private SenderType sender;

    @NotBlank
    @Size(max = 5000)
    private String content;

    @Size(max = 5000)
    private String context;

    public SenderType getSender() {
        return sender;
    }

    public void setSender(SenderType sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }
}
