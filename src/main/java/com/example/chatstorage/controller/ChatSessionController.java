package com.example.chatstorage.controller;

import com.example.chatstorage.dto.ChatMessageCreateRequest;
import com.example.chatstorage.dto.ChatMessageResponse;
import com.example.chatstorage.dto.ChatSessionCreateRequest;
import com.example.chatstorage.dto.ChatSessionFavoriteRequest;
import com.example.chatstorage.dto.ChatSessionRenameRequest;
import com.example.chatstorage.dto.ChatSessionResponse;
import com.example.chatstorage.dto.PagedResponse;
import com.example.chatstorage.service.ChatMessageService;
import com.example.chatstorage.service.ChatSessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class ChatSessionController {

    private final ChatSessionService chatSessionService;
    private final ChatMessageService chatMessageService;

    public ChatSessionController(ChatSessionService chatSessionService, ChatMessageService chatMessageService) {
        this.chatSessionService = chatSessionService;
        this.chatMessageService = chatMessageService;
    }

    @PostMapping("/sessions")
    @ResponseStatus(HttpStatus.CREATED)
    public ChatSessionResponse createSession(@Valid @RequestBody ChatSessionCreateRequest request) {
        return chatSessionService.createSession(request);
    }

    @GetMapping("/sessions")
    public List<ChatSessionResponse> listSessions(@RequestParam("userId") String userId) {
        return chatSessionService.listSessions(userId);
    }

    @PatchMapping("/sessions/{sessionId}/rename")
    public ChatSessionResponse renameSession(@PathVariable("sessionId") UUID sessionId,
                                             @RequestParam("userId") String userId,
                                             @Valid @RequestBody ChatSessionRenameRequest request) {
        return chatSessionService.renameSession(sessionId, userId, request.getTitle());
    }

    @PatchMapping("/sessions/{sessionId}/favorite")
    public ChatSessionResponse updateFavorite(@PathVariable("sessionId") UUID sessionId,
                                              @RequestParam("userId") String userId,
                                              @Valid @RequestBody ChatSessionFavoriteRequest request) {
        return chatSessionService.updateFavorite(sessionId, userId, request.getFavorite());
    }

    @DeleteMapping("/sessions/{sessionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSession(@PathVariable("sessionId") UUID sessionId, @RequestParam("userId") String userId) {
        chatSessionService.deleteSession(sessionId, userId);
    }

    @PostMapping("/sessions/{sessionId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public ChatMessageResponse addMessage(@PathVariable("sessionId") UUID sessionId,
                                          @RequestParam("userId") String userId,
                                          @Valid @RequestBody ChatMessageCreateRequest request) {
        return chatMessageService.addMessage(sessionId, userId, request);
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public PagedResponse<ChatMessageResponse> getMessages(@PathVariable("sessionId") UUID sessionId,
                                                          @RequestParam("userId") String userId,
                                                          @RequestParam(name = "page", defaultValue = "0") int page,
                                                          @RequestParam(name = "size", defaultValue = "20") int size) {
        return chatMessageService.getMessages(sessionId, userId, page, size);
    }
}
