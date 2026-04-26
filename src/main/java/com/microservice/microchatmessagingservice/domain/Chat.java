package com.microservice.microchatmessagingservice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    private LocalDateTime lastMessageAt;

    private String lastMessagePreview;

    private List<ChatParticipant> participants;

    public void addParticipants(List<Long> userIds) {

        this.participants = userIds.stream()
                .map(userId -> ChatParticipant.builder()
                        .userId(userId)
                        .lastReadAt(LocalDateTime.now())
                        .build())
                .toList();
    }
}
