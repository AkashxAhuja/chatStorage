package com.example.chatstorage.service;

import com.example.chatstorage.domain.ChatMessage;
import com.example.chatstorage.domain.ChatSession;
import com.example.chatstorage.dto.ChatMessageCreateRequest;
import com.example.chatstorage.dto.ChatMessageResponse;
import com.example.chatstorage.dto.PagedResponse;
import com.example.chatstorage.mapper.ChatMessageMapper;
import com.example.chatstorage.repository.ChatMessageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatSessionService chatSessionService;
    private final ChatMessageMapper chatMessageMapper;

    public ChatMessageService(ChatMessageRepository chatMessageRepository,
                              ChatSessionService chatSessionService,
                              ChatMessageMapper chatMessageMapper) {
        this.chatMessageRepository = chatMessageRepository;
        this.chatSessionService = chatSessionService;
        this.chatMessageMapper = chatMessageMapper;
    }

    @Transactional
    public ChatMessageResponse addMessage(UUID sessionId, String userId, ChatMessageCreateRequest request) {
        ChatSession session = chatSessionService.getSession(sessionId, userId);
        ChatMessage message = chatMessageMapper.toEntity(request);
        message.setSession(session);
        message.setCreatedAt(OffsetDateTime.now());
        ChatMessage saved = chatMessageRepository.save(message);
        session.setUpdatedAt(OffsetDateTime.now());
        return chatMessageMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ChatMessageResponse> getMessages(UUID sessionId, String userId, int page, int size) {
        ChatSession session = chatSessionService.getSession(sessionId, userId);
        if (page < 0) {
            throw new IllegalArgumentException("Page index must be greater than or equal to zero");
        }
        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }
        PageRequest pageable = PageRequest.of(page, size);
        Page<ChatMessage> result = chatMessageRepository.findBySessionOrderByCreatedAtAsc(session, pageable);
        return new PagedResponse<>(
                result.getContent().stream().map(chatMessageMapper::toResponse).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isLast()
        );
    }

    @Transactional
    public void deleteMessagesForSession(ChatSession session) {
        chatMessageRepository.deleteBySession(session);
    }
}
