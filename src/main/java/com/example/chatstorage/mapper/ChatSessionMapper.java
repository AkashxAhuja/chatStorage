package com.example.chatstorage.mapper;

import com.example.chatstorage.domain.ChatSession;
import com.example.chatstorage.dto.ChatSessionCreateRequest;
import com.example.chatstorage.dto.ChatSessionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ChatSessionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "favorite", constant = "false")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    ChatSession toEntity(ChatSessionCreateRequest request);

    ChatSessionResponse toResponse(ChatSession session);

    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(ChatSessionCreateRequest request, @MappingTarget ChatSession session);
}
