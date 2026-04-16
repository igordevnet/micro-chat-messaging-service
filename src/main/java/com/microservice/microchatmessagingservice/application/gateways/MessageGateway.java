package com.microservice.microchatmessagingservice.application.gateways;

import com.microservice.microchatmessagingservice.domain.Message;

import java.util.List;
import java.util.UUID;

public interface MessageGateway {

    Message saveMessage(Message message);

    void deleteMessage(String messageId);

    Message updateMessage(Message newMessage);

    List<Message> getMessageList(UUID chatId);
}
