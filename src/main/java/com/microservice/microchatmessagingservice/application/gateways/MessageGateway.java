package com.microservice.microchatmessagingservice.application.gateways;

import com.microservice.microchatmessagingservice.domain.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface MessageGateway {

    Message saveMessage(Message message);

    void deleteMessage(String messageId);

    Message updateMessage(Message newMessage);

    Page<Message> getMessagePage(UUID chatId, Pageable pageable);

    Optional<Message> findMessageById(String messageId);

    Optional<Message> findLastMessageByChatId(UUID chatId);
}
