package com.microservice.microchatmessagingservice.infrastructure.config.websockets;

import com.microservice.microchatmessagingservice.application.gateways.MessageBrokerGateway;
import com.microservice.microchatmessagingservice.application.gateways.RedisPresenceGateway;
import com.microservice.microchatmessagingservice.controller.dtos.response.UserStatusEvent;
import com.microservice.microchatmessagingservice.domain.enums.Status;
import com.microservice.microchatmessagingservice.infrastructure.config.UserAuthenticated;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final MessageBrokerGateway messageBrokerGateway;
    private final RedisPresenceGateway redisPresenceGateway;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        Long userId = extractUserId(event.getUser());
        if (userId != null) {
            log.info("User Connected: {}", userId);

            redisPresenceGateway.setUserOnline(userId);

            broadcastStatus(userId, Status.ONLINE);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        Long userId = extractUserId(event.getUser());
        if (userId != null) {
            log.info("User Disconnected: {}", userId);

            redisPresenceGateway.setUserOffline(userId);

            broadcastStatus(userId, Status.OFFLINE);
        }
    }

    private Long extractUserId(Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken auth) {
            if (auth.getPrincipal() instanceof UserAuthenticated currentUser) {
                return currentUser.id();
            }
        }
        return null;
    }

    private void broadcastStatus(Long userId, Status status) {
        var statusEvent = new UserStatusEvent(userId, status);

        messageBrokerGateway.convertAndSend("chat.topic", "system.presence", statusEvent);
    }
}
