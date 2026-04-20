package com.microservice.microchatmessagingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@SpringBootApplication
@EnableWebSocket
@EnableMongoAuditing
@EnableCaching
public class MicroChatMessagingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MicroChatMessagingServiceApplication.class, args);
    }

}
