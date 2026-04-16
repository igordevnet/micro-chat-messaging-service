package com.microservice.microchatmessagingservice.infrastructure.persistence.postgre;

import com.microservice.microchatmessagingservice.infrastructure.persistence.postgre.entities.ChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatRepository extends JpaRepository<ChatEntity, UUID> {

    List<ChatEntity> findAllByParticipants(Long userId);
}
