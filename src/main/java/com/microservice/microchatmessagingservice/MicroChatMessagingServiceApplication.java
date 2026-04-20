package com.microservice.microchatmessagingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@SpringBootApplication
@EnableWebSocket
@EnableMongoAuditing
public class MicroChatMessagingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MicroChatMessagingServiceApplication.class, args);
    }

}
