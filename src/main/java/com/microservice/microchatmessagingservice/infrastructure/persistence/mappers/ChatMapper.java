package com.microservice.microchatmessagingservice.infrastructure.persistence.mappers;

import com.microservice.microchatmessagingservice.controller.dtos.response.ChatResponse;
import com.microservice.microchatmessagingservice.controller.dtos.request.ChatRequest;
import com.microservice.microchatmessagingservice.domain.Chat;
import com.microservice.microchatmessagingservice.infrastructure.persistence.postgre.entities.ChatEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChatMapper {
    Chat entityToDomain(ChatEntity chatEntity);

    ChatEntity domainToEntity(Chat chat);

    Chat requestToDomain(ChatRequest chatRequest);

    ChatResponse domainToResponse(Chat chat);

    List<ChatResponse> domainToResponseList(List<Chat> chats);

    List<Chat> entityToDomainList(List<ChatEntity> chatEntities);
}
