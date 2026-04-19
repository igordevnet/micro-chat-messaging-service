package com.microservice.microchatmessagingservice.infrastructure.gateways;

import com.microservice.microchatmessagingservice.application.gateways.MessageGateway;
import com.microservice.microchatmessagingservice.domain.Message;
import com.microservice.microchatmessagingservice.infrastructure.persistence.mappers.MessageMapper;
import com.microservice.microchatmessagingservice.infrastructure.persistence.mongodb.MessageRepository;
import com.microservice.microchatmessagingservice.infrastructure.persistence.mongodb.entities.MessageEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageRepositoryGateway implements MessageGateway {

    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;

    @Override
    public Message saveMessage(Message message) {
        MessageEntity messageEntity = messageMapper.domainToEntity(message);

        var savedMessage = messageRepository.save(messageEntity);

        return messageMapper.entityToDomain(savedMessage);
    }

    @Override
    public void deleteMessage(String messageId) {
        messageRepository.deleteById(messageId);
    }

    @Override
    public Message updateMessage(Message newMessage) {
        MessageEntity messageEntity = messageMapper.domainToEntity(newMessage);
        messageEntity.setEdited(true);

        var savedMessage = messageRepository.save(messageEntity);

        return messageMapper.entityToDomain(savedMessage);
    }

    @Override
    public Page<Message> getMessagePage(UUID chatId, Pageable pageable) {
        Page<MessageEntity> entityPage = messageRepository.findByChatIdOrderByCreatedAtDesc(chatId, pageable);

        return entityPage.map(messageMapper::entityToDomain);
    }

    @Override
    public Optional<Message> findMessageById(String messageId) {
        Optional<MessageEntity> messageEntity = messageRepository.findById(messageId);

        return messageEntity.map(messageMapper::entityToDomain);
    }
}
