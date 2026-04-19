package com.microservice.microchatmessagingservice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Message {
    private String id;

    private UUID chatId;
    private Long senderId;
    private String content;
    private Boolean edited;
    private Boolean read;

    private LocalDateTime timestamp;
}
