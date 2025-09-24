package com.example.chatstorage.service;

import com.example.chatstorage.domain.ChatSession;
import com.example.chatstorage.dto.ChatSessionCreateRequest;
import com.example.chatstorage.dto.ChatSessionResponse;
import com.example.chatstorage.mapper.ChatSessionMapper;
import com.example.chatstorage.repository.ChatSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ChatSessionService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatSessionMapper chatSessionMapper;

    public ChatSessionService(ChatSessionRepository chatSessionRepository, ChatSessionMapper chatSessionMapper) {
        this.chatSessionRepository = chatSessionRepository;
        this.chatSessionMapper = chatSessionMapper;
    }

    @Transactional
    public ChatSessionResponse createSession(ChatSessionCreateRequest request) {
        ChatSession session = chatSessionMapper.toEntity(request);
        OffsetDateTime now = OffsetDateTime.now();
        session.setCreatedAt(now);
        session.setUpdatedAt(now);
        ChatSession saved = chatSessionRepository.save(session);
        return chatSessionMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ChatSessionResponse> listSessions(String userId) {
        return chatSessionRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(chatSessionMapper::toResponse)
                .toList();
    }

    @Transactional
    public ChatSessionResponse renameSession(UUID sessionId, String userId, String title) {
        ChatSession session = getSession(sessionId, userId);
        session.setTitle(title);
        session.setUpdatedAt(OffsetDateTime.now());
        return chatSessionMapper.toResponse(session);
    }

    @Transactional
    public ChatSessionResponse updateFavorite(UUID sessionId, String userId, boolean favorite) {
        ChatSession session = getSession(sessionId, userId);
        session.setFavorite(favorite);
        session.setUpdatedAt(OffsetDateTime.now());
        return chatSessionMapper.toResponse(session);
    }

    @Transactional
    public void deleteSession(UUID sessionId, String userId) {
        ChatSession session = getSession(sessionId, userId);
        chatSessionRepository.delete(session);
    }

    @Transactional(readOnly = true)
    public ChatSession getSession(UUID sessionId, String userId) {
        return chatSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
    }
}
