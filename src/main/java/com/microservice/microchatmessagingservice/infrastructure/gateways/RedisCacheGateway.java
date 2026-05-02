package com.microservice.microchatmessagingservice.infrastructure.gateways;

import com.microservice.microchatmessagingservice.application.gateways.CacheGateway;
import com.microservice.microchatmessagingservice.domain.enums.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RedisCacheGateway implements CacheGateway {

    private final StringRedisTemplate redisTemplate;
    private static final String PRESENCE_PREFIX = "user:presence:";
    private static final String TOKEN_PREFIX = "user:token:";

    @Override
    public void setUserOnline(Long userId) {
        redisTemplate.opsForValue().set(
                PRESENCE_PREFIX + userId,
                Status.ONLINE.name(),
                Duration.ofHours(24)
        );
    }

    @Override
    public void setUserOffline(Long userId) {
        redisTemplate.delete(PRESENCE_PREFIX + userId);
    }

    @Override
    public Map<Long, Status> getUsersPresence(List<Long> userIds) {
        return userIds.stream().collect(Collectors.toMap(
                id -> id,
                id -> {
                    String statusString = redisTemplate.opsForValue().get(PRESENCE_PREFIX + id);
                    return statusString != null ? Status.valueOf(statusString) : Status.OFFLINE;
                }
        ));
    }

    @Override
    public boolean isBlacklisted(String accessToken) {
        String result = redisTemplate.opsForValue().get(TOKEN_PREFIX + accessToken);
        return result != null;
    }
}
