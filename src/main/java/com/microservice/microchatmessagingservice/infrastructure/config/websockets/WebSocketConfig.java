package com.microservice.microchatmessagingservice.infrastructure.config.websockets;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${spring.rabbitmq.host}")
    private String rabbitmqHost;

    @Value("${spring.rabbitmq.port}")
    private String rabbitmqPort;

    @Value("${spring.rabbitmq.client.login}")
    private String rabbitmqClientLogin;

    @Value("${spring.rabbitmq.client.password}")
    private String rabbitmqClientPassword;

    @Value("${spring.rabbitmq.system.login}")
    private String rabbitmqSystemLogin;

    @Value("${spring.rabbitmq.system.password}")
    private String rabbitmqSystemPassword;

    private final WebSocketJwtInterceptor webSocketJwtInterceptor;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableStompBrokerRelay("/topic", "/queue")
                .setRelayHost(rabbitmqHost)
                .setRelayPort(Integer.parseInt(rabbitmqPort))
                .setClientLogin(rabbitmqClientLogin)
                .setClientPasscode(rabbitmqClientPassword)
                .setSystemLogin(rabbitmqSystemLogin)
                .setSystemPasscode(rabbitmqSystemPassword);

        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketJwtInterceptor);
    }
}
