package com.microservice.microchatmessagingservice.infrastructure.persistence.mappers;

import com.microservice.microchatmessagingservice.controller.dtos.reponse.MessageResponse;
import com.microservice.microchatmessagingservice.controller.dtos.request.EditMessageRequest;
import com.microservice.microchatmessagingservice.controller.dtos.request.SendMessageRequest;
import com.microservice.microchatmessagingservice.domain.Message;
import com.microservice.microchatmessagingservice.infrastructure.persistence.mongodb.entities.MessageEntity;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MessageMapper {
    Message entityToDomain(MessageEntity messageEntity);

    MessageEntity domainToEntity(Message message);

    Message sendRequestToDomain(SendMessageRequest sendMessageRequest);

    Message editRequestToDomain(EditMessageRequest editMessageRequest);

    MessageResponse domainToResponse(Message message);
}
