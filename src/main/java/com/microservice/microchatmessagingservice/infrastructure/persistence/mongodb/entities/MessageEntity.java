package com.microservice.microchatmessagingservice.infrastructure.persistence.mongodb.entities;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Document(collection = "messages")
public class MessageEntity {

    @Id
    private String id;

    private UUID chatId;
    private UUID senderId;
    private String content;
    private LocalDateTime timestamp;
}
