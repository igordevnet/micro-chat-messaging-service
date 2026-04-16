package com.microservice.microchatmessagingservice.infrastructure.persistence.mappers;

import com.microservice.microchatmessagingservice.domain.Chat;
import com.microservice.microchatmessagingservice.infrastructure.persistence.postgre.entities.ChatEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChatMapper {
    Chat entityToDomain(ChatEntity chatEntity);

    ChatEntity domainToEntity(Chat chat);

    List<Chat> entityToList(List<ChatEntity> chatEntities);
}
