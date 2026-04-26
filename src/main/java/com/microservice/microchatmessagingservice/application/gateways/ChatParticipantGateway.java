package com.microservice.microchatmessagingservice.application.gateways;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ChatParticipantGateway {
    int updateLastReadAt(UUID chatId, Long userId, LocalDateTime now);
}
