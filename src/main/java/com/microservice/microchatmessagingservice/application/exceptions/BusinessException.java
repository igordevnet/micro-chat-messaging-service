package com.microservice.microchatmessagingservice.application.exceptions;

public abstract class BusinessException extends RuntimeException{

    protected BusinessException(String message){
        super(message);
    }
}
