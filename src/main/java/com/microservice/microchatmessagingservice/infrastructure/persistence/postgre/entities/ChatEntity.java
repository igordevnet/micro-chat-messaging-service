package com.microservice.microchatmessagingservice.infrastructure.persistence.postgre.entities;

import com.microservice.microchatmessagingservice.domain.ChatType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "chats", indexes = {
        @Index(name = "idx_last_message_at", columnList = "lastMessageAt")
})
public class ChatEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String chatName;

    @Enumerated(EnumType.STRING)
    private ChatType type;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Instant lastMessageAt;

    private String lastMessagePreview;

    @ElementCollection
    @CollectionTable(
            name = "chat_participants",
            joinColumns = @JoinColumn(name = "chat_id"),
            indexes = @Index(name = "idx_participants_user_id", columnList = "user_id")
    )
    @Column(name = "user_id")
    private List<Long> participants;
}
