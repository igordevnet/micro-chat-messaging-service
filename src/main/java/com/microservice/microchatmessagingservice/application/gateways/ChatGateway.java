package com.microservice.microchatmessagingservice.application.gateways;

import com.microservice.microchatmessagingservice.domain.Chat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatGateway {

    Chat saveChat(Chat chat);

    void deleteChat(UUID chatId);

    List<Chat> getChatList(Long userId);

    Optional<Chat> getChat(UUID chatId);

    void updateLastMessage(UUID chatId, String preview, LocalDateTime time);

    void forceUpdateLastMessagePreview(UUID chatId, String preview, LocalDateTime time);
}
