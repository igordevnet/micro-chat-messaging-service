package com.microservice.microchatmessagingservice.controller.handler;

import com.microservice.microchatmessagingservice.application.exceptions.InvalidTokenException;
import com.microservice.microchatmessagingservice.application.exceptions.MessageNotFoundException;
import com.microservice.microchatmessagingservice.application.exceptions.UnauthorizedActionException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDate;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<StandardError> handleInvalidTokenException(InvalidTokenException ex, HttpServletRequest request) {
        var response = StandardError.builder()
                .error(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDate.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .build();

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MessageNotFoundException.class)
    public ResponseEntity<StandardError> handleMessageNotFoundException(MessageNotFoundException ex, HttpServletRequest request) {
        var response = StandardError.builder()
                .error(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDate.now())
                .status(HttpStatus.NOT_FOUND.value())
                .build();

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UnauthorizedActionException.class)
    public ResponseEntity<StandardError> handleUnauthorizedActionException(UnauthorizedActionException ex, HttpServletRequest request) {
        var response = StandardError.builder()
                .error(ex.getMessage())
                .path(request.getRequestURI())
                .timestamp(LocalDate.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .build();

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardError> handleValidationErrors(MethodArgumentNotValidException e, HttpServletRequest request) {
        String errorMessage = e.getBindingResult().getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .reduce("", (acc, error) -> acc + error + "; ");

        var response = StandardError.builder()
                .error("Error validating: " + errorMessage)
                .timestamp(LocalDate.now())
                .path(request.getRequestURI())
                .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .build();

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }
}
