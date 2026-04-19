package com.microservice.microchatmessagingservice.application.exceptions;

public class MessageNotFoundException extends BusinessException {
    public MessageNotFoundException(String message) {
        super(message);
    }
}
