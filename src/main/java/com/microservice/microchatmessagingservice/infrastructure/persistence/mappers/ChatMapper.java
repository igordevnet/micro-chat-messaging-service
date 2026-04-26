package com.microservice.microchatmessagingservice.infrastructure.persistence.mappers;

import com.microservice.microchatmessagingservice.controller.dtos.response.ChatResponse;
import com.microservice.microchatmessagingservice.controller.dtos.request.ChatRequest;
import com.microservice.microchatmessagingservice.domain.Chat;
import com.microservice.microchatmessagingservice.domain.ChatParticipant;
import com.microservice.microchatmessagingservice.infrastructure.persistence.postgre.entities.ChatEntity;
import com.microservice.microchatmessagingservice.infrastructure.persistence.postgre.entities.ChatParticipantEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChatMapper {
    Chat entityToDomain(ChatEntity chatEntity);

    ChatEntity domainToEntity(Chat chat);

    Chat requestToDomain(ChatRequest chatRequest);

    ChatResponse domainToResponse(Chat chat);

    @Mapping(target = "chat", ignore = true)
    ChatParticipantEntity domainToParticipantEntity(ChatParticipant participant);

    @Mapping(target = "chatId", source = "chat.id")
    ChatParticipant entityToDomain(ChatParticipantEntity entity);

    List<ChatParticipantEntity> domainToParticipantEntityList(List<ChatParticipant> participants);

    List<ChatResponse> domainToResponseList(List<Chat> chats);

    List<Chat> entityToDomainList(List<ChatEntity> chatEntities);

}
