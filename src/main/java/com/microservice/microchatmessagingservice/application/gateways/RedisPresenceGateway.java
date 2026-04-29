package com.microservice.microchatmessagingservice.application.gateways;

import com.microservice.microchatmessagingservice.domain.enums.Status;

import java.util.List;
import java.util.Map;

public interface RedisPresenceGateway {
    void setUserOnline(Long userId);

    void setUserOffline(Long userId);

    Map<Long, Status> getUsersPresence(List<Long> userIds);
}
