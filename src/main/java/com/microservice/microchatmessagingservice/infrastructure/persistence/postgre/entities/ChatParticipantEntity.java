package com.microservice.microchatmessagingservice.infrastructure.persistence.postgre.entities;

import com.microservice.microchatmessagingservice.domain.Chat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "chat_participants", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"chat_id", "user_id"})
})
public class ChatParticipantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    private ChatEntity chat;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    private LocalDateTime lastReadAt;
}
