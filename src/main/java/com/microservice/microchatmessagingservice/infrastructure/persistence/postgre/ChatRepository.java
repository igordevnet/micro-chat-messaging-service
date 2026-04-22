package com.microservice.microchatmessagingservice.infrastructure.persistence.postgre;

import com.microservice.microchatmessagingservice.infrastructure.persistence.postgre.entities.ChatEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ChatRepository extends JpaRepository<ChatEntity, UUID> {

    @Query("SELECT DISTINCT c FROM ChatEntity c JOIN c.participants p WHERE p.userId = :userId")
    List<ChatEntity> findAllByParticipants(Long userId);

    @Modifying
    @Transactional
    @Query("""
        UPDATE ChatEntity c 
        SET c.lastMessagePreview = :preview, 
            c.lastMessageAt = :time 
        WHERE c.id = :chatId 
          AND (c.lastMessageAt IS NULL OR c.lastMessageAt < :time)
    """)
    int updateLastMessageIfNewer(
            @Param("chatId") UUID chatId,
            @Param("preview") String preview,
            @Param("time") LocalDateTime time
    );

    @Modifying
    @Transactional
    @Query("""
        UPDATE ChatEntity c 
        SET c.lastMessagePreview = :preview, 
            c.lastMessageAt = :time 
        WHERE c.id = :chatId 
    """)
    void forceUpdateLastMessage(
            @Param("chatId") UUID chatId,
            @Param("preview") String preview,
            @Param("time") LocalDateTime time
    );
}
