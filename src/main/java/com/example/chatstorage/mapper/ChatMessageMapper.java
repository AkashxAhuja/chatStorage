package com.example.chatstorage.mapper;

import com.example.chatstorage.domain.ChatMessage;
import com.example.chatstorage.dto.ChatMessageCreateRequest;
import com.example.chatstorage.dto.ChatMessageResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ChatMessageMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "session", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    ChatMessage toEntity(ChatMessageCreateRequest request);

    ChatMessageResponse toResponse(ChatMessage message);
}
