package com.microservice.microchatmessagingservice.infrastructure.gateways;

import com.microservice.microchatmessagingservice.application.gateways.ChatGateway;
import com.microservice.microchatmessagingservice.domain.Chat;
import com.microservice.microchatmessagingservice.infrastructure.persistence.mappers.ChatMapper;
import com.microservice.microchatmessagingservice.infrastructure.persistence.postgre.ChatRepository;
import com.microservice.microchatmessagingservice.infrastructure.persistence.postgre.entities.ChatEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatRepositoryGateway implements ChatGateway {

    private final ChatRepository chatRepository;
    private final ChatMapper chatMapper;

    @Override
    public Chat saveChat(Chat chat) {
        ChatEntity chatEntity = chatMapper.domainToEntity(chat);

        var savedChat = chatRepository.save(chatEntity);

        return chatMapper.entityToDomain(savedChat);
    }

    @Override
    public void deleteChat(UUID chatId) {
        chatRepository.deleteById(chatId);
    }

    @Override
    public List<Chat> getChatList(Long userId) {
        List<ChatEntity> chatEntities = chatRepository.findAllByParticipants(userId);

        return chatMapper.entityToDomainList(chatEntities);
    }

    @Override
    public Optional<Chat> getChat(UUID chatId) {
        return chatRepository.findById(chatId)
                .map(chatMapper::entityToDomain);
    }

    @Override
    public void updateLastMessage(UUID chatId, String preview, LocalDateTime time) {
        chatRepository.updateLastMessageIfNewer(chatId, preview, time);
    }

    @Override
    public void forceUpdateLastMessagePreview(UUID chatId, String preview, LocalDateTime time) {
        chatRepository.forceUpdateLastMessage(chatId, preview, time);
    }
}
