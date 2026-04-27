package com.microservice.microchatmessagingservice.application.exceptions;

public class FailedUploadException extends BusinessException {
    public FailedUploadException(String message) {
        super(message);
    }
}
