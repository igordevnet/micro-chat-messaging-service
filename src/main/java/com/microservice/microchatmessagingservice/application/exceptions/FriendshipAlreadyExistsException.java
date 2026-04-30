package com.microservice.microchatmessagingservice.application.exceptions;

public class FriendshipAlreadyExistsException extends BusinessException  {
    public FriendshipAlreadyExistsException(String message) {
        super(message);
    }
}
