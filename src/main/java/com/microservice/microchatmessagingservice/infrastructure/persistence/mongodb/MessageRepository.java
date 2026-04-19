package com.microservice.microchatmessagingservice.infrastructure.persistence.mongodb;

import com.microservice.microchatmessagingservice.infrastructure.persistence.mongodb.entities.MessageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends MongoRepository<MessageEntity, String> {

    Page<MessageEntity> findByChatIdOrderByCreatedAtDesc(UUID chatId, Pageable pageable);
}
