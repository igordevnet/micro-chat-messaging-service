package com.microservice.microchatmessagingservice.infrastructure.config;

import com.microservice.microchatmessagingservice.application.exceptions.InvalidTokenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketJwtInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            String authorizationHeader = accessor.getFirstNativeHeader("Authorization");

            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7);

                if (jwtService.isValid(token)) {
                    Long userId = jwtService.extractUserId(token);
                    String username = jwtService.extractUsername(token);
                    String role = jwtService.extractRole(token);

                    var user = new UserAuthenticated(userId, username, role);
                    var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

                    accessor.setUser(authentication);
                } else {

                    log.error("Invalid token");

                    return null;
                }
            }
        }
        return message;
    }
}
