package com.microservice.microchatmessagingservice.infrastructure.persistence.mongodb.entities;

import com.microservice.microchatmessagingservice.domain.enums.MessageType;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
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
    private Long senderId;
    private String content;
    private boolean edited;

    private AttachmentEntity attachment;
    private MessageType messageType;

    @CreatedDate
    private LocalDateTime createdAt;
}
