package com.microservice.microchatmessagingservice.application.usecases;

import com.microservice.microchatmessagingservice.application.exceptions.UnauthorizedActionException;
import com.microservice.microchatmessagingservice.application.gateways.ChatGateway;
import com.microservice.microchatmessagingservice.controller.dtos.reponse.ChatResponse;
import com.microservice.microchatmessagingservice.controller.dtos.request.ChatRequest;
import com.microservice.microchatmessagingservice.domain.Chat;
import com.microservice.microchatmessagingservice.infrastructure.persistence.mappers.ChatMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatUseCase {

    private final ChatGateway chatGateway;
    private final ChatMapper chatMapper;

    public ChatResponse createChat(ChatRequest chatRequest) {
        Chat chat = chatMapper.requestToDomain(chatRequest);

        Chat savedChat = chatGateway.saveChat(chat);

        return chatMapper.domainToResponse(savedChat);
    }

    public ChatResponse updateChat(UUID chatId, ChatRequest chatRequest) {
        Chat chat = chatMapper.requestToDomain(chatRequest);
        chat.setUpdatedAt(LocalDateTime.now());
        chat.setId(chatId);

        Chat savedChat = chatGateway.saveChat(chat);

        return chatMapper.domainToResponse(savedChat);
    }

    public void deleteChat(
            Long userId,
            UUID chatId
    ) {
        Chat chat = chatGateway.getChat(chatId)
                        .orElseThrow();

        throwIfUserCannotDeleteChat(userId, chat.getParticipants());

        chatGateway.deleteChat(chatId);
    }

    public List<ChatResponse> getAllChats(Long userId) {
        List<Chat> chats = chatGateway.getChatList(userId);

        return chatMapper.domainToResponseList(chats);
    }

    private void throwIfUserCannotDeleteChat(Long userId, List<Long> participants) {
        if (!participants.contains(userId)) {
            throw new UnauthorizedActionException("You are not allowed to delete this chat");
        }
    }
}
