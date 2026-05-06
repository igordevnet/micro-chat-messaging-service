package com.microservice.microchatmessagingservice.application.exceptions;

public class ChatNotFoundException extends BusinessException {
    public ChatNotFoundException(String message) {
        super(message);
    }
}
