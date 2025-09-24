package com.example.chatstorage.service;

import com.example.chatstorage.domain.ChatSession;
import com.example.chatstorage.dto.ChatSessionCreateRequest;
import com.example.chatstorage.dto.ChatSessionResponse;
import com.example.chatstorage.mapper.ChatSessionMapper;
import com.example.chatstorage.repository.ChatSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChatSessionServiceTest {

    private ChatSessionRepository chatSessionRepository;
    private ChatSessionMapper chatSessionMapper;
    private ChatSessionService chatSessionService;

    @BeforeEach
    void setUp() {
        chatSessionRepository = mock(ChatSessionRepository.class);
        chatSessionMapper = new TestChatSessionMapper();
        chatSessionService = new ChatSessionService(chatSessionRepository, chatSessionMapper);
    }

    @Test
    void createSessionShouldPersistAndReturnResponse() {
        ChatSessionCreateRequest request = new ChatSessionCreateRequest();
        request.setUserId("user-1");
        request.setTitle("New Session");

        ArgumentCaptor<ChatSession> captor = ArgumentCaptor.forClass(ChatSession.class);
        when(chatSessionRepository.save(captor.capture())).thenAnswer(invocation -> {
            ChatSession session = invocation.getArgument(0);
            session.setId(UUID.randomUUID());
            return session;
        });

        ChatSessionResponse response = chatSessionService.createSession(request);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getTitle()).isEqualTo("New Session");
        assertThat(captor.getValue().getCreatedAt()).isNotNull();
        assertThat(captor.getValue().getUpdatedAt()).isNotNull();
    }

    @Test
    void getSessionShouldThrowWhenMissing() {
        UUID sessionId = UUID.randomUUID();
        when(chatSessionRepository.findByIdAndUserId(sessionId, "user-1")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> chatSessionService.getSession(sessionId, "user-1"));
    }

    private static class TestChatSessionMapper implements ChatSessionMapper {

        @Override
        public ChatSession toEntity(ChatSessionCreateRequest request) {
            ChatSession session = new ChatSession();
            session.setTitle(request.getTitle());
            session.setUserId(request.getUserId());
            session.setFavorite(false);
            return session;
        }

        @Override
        public ChatSessionResponse toResponse(ChatSession session) {
            ChatSessionResponse response = new ChatSessionResponse();
            response.setId(session.getId());
            response.setTitle(session.getTitle());
            response.setUserId(session.getUserId());
            response.setFavorite(session.isFavorite());
            response.setCreatedAt(session.getCreatedAt());
            response.setUpdatedAt(session.getUpdatedAt());
            return response;
        }

        @Override
        public void updateEntityFromRequest(ChatSessionCreateRequest request, ChatSession session) {
            session.setTitle(request.getTitle());
            session.setUserId(request.getUserId());
            session.setUpdatedAt(OffsetDateTime.now());
        }
    }
}
