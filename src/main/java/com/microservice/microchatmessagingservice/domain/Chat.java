package com.microservice.microchatmessagingservice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Chat {

    private UUID id;

    private String chatName;

    private ChatType type;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Instant lastMessageAt;

    private String lastMessagePreview;

    private List<Long> participants;
}
