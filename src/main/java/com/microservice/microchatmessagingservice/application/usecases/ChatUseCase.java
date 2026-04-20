package com.microservice.microchatmessagingservice.application.usecases;

import com.microservice.microchatmessagingservice.application.gateways.ChatGateway;
import com.microservice.microchatmessagingservice.controller.dtos.reponse.ChatResponse;
import com.microservice.microchatmessagingservice.controller.dtos.request.ChatRequest;
import com.microservice.microchatmessagingservice.domain.Chat;
import com.microservice.microchatmessagingservice.infrastructure.persistence.mappers.ChatMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatUseCase implements Serializable {

    private final ChatGateway chatGateway;
    private final ChatMapper chatMapper;

    @CacheEvict(value = "userChats", key = "#chatRequest.userId")
    public ChatResponse createChat(ChatRequest chatRequest) {
        Chat chat = chatMapper.requestToDomain(chatRequest);

        Chat savedChat = chatGateway.saveChat(chat);

        return chatMapper.domainToResponse(savedChat);
    }

    @CacheEvict(value = "userChats", allEntries = true)
    public ChatResponse updateChat(UUID chatId, ChatRequest chatRequest) {
        Chat chat = chatMapper.requestToDomain(chatRequest);
        chat.setUpdatedAt(LocalDateTime.now());
        chat.setId(chatId);

        Chat savedChat = chatGateway.saveChat(chat);

        return chatMapper.domainToResponse(savedChat);
    }

    @CacheEvict(value = "userChats", key = "#userId")
    public void deleteChat(
            Long userId,
            UUID chatId
    ) {
        Optional<Chat> chatOptional = chatGateway.getChat(chatId);

        if (chatOptional.isEmpty()) {
            return;
        }

        Chat chat = chatOptional.get();

        if (!chat.getParticipants().contains(userId)) {
            log.warn("SECURITY ALERT: User {} attempted to delete chat {} without permissions.", userId, chatId);
            return;
        }

        chatGateway.deleteChat(chatId);
    }

    @Cacheable(value = "userChats", key = "#userId")
    public List<ChatResponse> getAllChats(Long userId) {
        List<Chat> chats = chatGateway.getChatList(userId);

        return chatMapper.domainToResponseList(chats);
    }
}
