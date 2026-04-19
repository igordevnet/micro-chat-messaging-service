package com.microservice.microchatmessagingservice.application.gateways;

import com.microservice.microchatmessagingservice.domain.Chat;

import java.util.List;
import java.util.UUID;

public interface ChatGateway {

    Chat saveChat(Chat chat);

    void deleteChat(UUID chatId);

    List<Chat> getChatList(Long userId);
}
