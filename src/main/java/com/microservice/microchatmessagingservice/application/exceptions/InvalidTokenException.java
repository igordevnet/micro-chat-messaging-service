package com.microservice.microchatmessagingservice.application.exceptions;

public class InvalidTokenException extends BusinessException {
    public InvalidTokenException(String message) {
        super(message);
    }
}
