package com.microservice.microchatmessagingservice.application.exceptions;

public class FriendshipNotFound extends BusinessException {
    public FriendshipNotFound(String message) {
        super(message);
    }
}
