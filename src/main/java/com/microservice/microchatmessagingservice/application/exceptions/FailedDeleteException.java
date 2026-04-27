package com.microservice.microchatmessagingservice.application.exceptions;

public class FailedDeleteException extends BusinessException {
    public FailedDeleteException(String message) {
        super(message);
    }
}
