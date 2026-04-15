package com.microservice.microchatmessagingservice.infrastructure.persistence.postgre;

import com.microservice.microchatmessagingservice.infrastructure.persistence.postgre.entities.ChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ChatRepository extends JpaRepository<ChatEntity, UUID> {
}
