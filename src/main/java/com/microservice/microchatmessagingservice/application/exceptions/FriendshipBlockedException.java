package com.microservice.microchatmessagingservice.application.exceptions;

public class FriendshipBlockedException extends BusinessException {
    public FriendshipBlockedException(String message) {
        super(message);
    }
}
