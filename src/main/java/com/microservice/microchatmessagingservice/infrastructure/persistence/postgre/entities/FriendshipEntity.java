package com.microservice.microchatmessagingservice.infrastructure.persistence.postgre.entities;

import com.microservice.microchatmessagingservice.domain.enums.FriendshipStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "friendship", indexes = {
        @Index(name = "idx_requester_id", columnList = "requester_id"),
        @Index(name = "idx_receiver_id", columnList = "receiver_id")
})
public class FriendshipEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "requester_id", nullable = false)
    private Long requesterId;

    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendshipStatus status;
}
