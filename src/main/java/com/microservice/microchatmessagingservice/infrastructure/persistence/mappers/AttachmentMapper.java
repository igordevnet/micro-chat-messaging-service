package com.microservice.microchatmessagingservice.infrastructure.persistence.mappers;

import com.microservice.microchatmessagingservice.controller.dtos.response.AttachmentResponse;
import com.microservice.microchatmessagingservice.domain.Attachment;
import com.microservice.microchatmessagingservice.infrastructure.persistence.mongodb.entities.AttachmentEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AttachmentMapper {
    AttachmentEntity domainToEntity(Attachment attachment);
    Attachment entityToDomain(AttachmentEntity entity);
    AttachmentResponse domainToResponse(Attachment attachment);
}
