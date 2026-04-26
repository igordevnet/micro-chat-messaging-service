package com.microservice.microchatmessagingservice.infrastructure.persistence.postgre;

import com.microservice.microchatmessagingservice.infrastructure.persistence.postgre.entities.ChatParticipantEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipantEntity, Long> {

    @Modifying
    @Transactional
    @Query("""
        UPDATE ChatParticipantEntity cp 
        SET cp.lastReadAt = :readTime 
        WHERE cp.chat.id = :chatId 
          AND cp.userId = :userId 
          AND (cp.lastReadAt IS NULL OR cp.lastReadAt < :readTime)
    """)
    int updateLastReadAt(@Param("chatId") UUID chatId,
                         @Param("userId") Long userId,
                         @Param("readTime") LocalDateTime readTime);

    Optional<ChatParticipantEntity> findByChatIdAndUserId(UUID chatId, Long userId);
}
