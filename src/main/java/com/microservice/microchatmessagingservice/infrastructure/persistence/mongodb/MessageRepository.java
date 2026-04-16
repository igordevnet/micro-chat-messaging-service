package com.microservice.microchatmessagingservice.infrastructure.persistence.mongodb;

import com.microservice.microchatmessagingservice.infrastructure.persistence.mongodb.entities.MessageEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MessageRepository extends MongoRepository<MessageEntity, String> {
}
