package com.microservice.microchatmessagingservice.infrastructure.persistence.mappers;

import com.microservice.microchatmessagingservice.domain.Message;
import com.microservice.microchatmessagingservice.infrastructure.persistence.mongodb.entities.MessageEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MessageMapper {
    Message entityToDomain(MessageEntity messageEntity);

    MessageEntity domainToEntity(Message message);

    List<Message> entityToList(List<MessageEntity> messageEntities);
}
