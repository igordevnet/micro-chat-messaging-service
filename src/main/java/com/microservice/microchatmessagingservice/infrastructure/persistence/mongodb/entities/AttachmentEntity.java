package com.microservice.microchatmessagingservice.infrastructure.persistence.mongodb.entities;

import lombok.Data;

@Data
public class AttachmentEntity {
    private String id;
    private String fileName;
    private String contentType;
    private Long size;
    private String key;
    private Double duration;
}
