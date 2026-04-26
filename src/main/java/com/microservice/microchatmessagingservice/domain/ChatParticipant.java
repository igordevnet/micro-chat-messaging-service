package com.microservice.microchatmessagingservice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatParticipant {
    private Long id;

    private UUID chatId;

    private Long userId;

    private LocalDateTime lastReadAt;
}
