package com.example.kafkaproducerhomework.exceptionhandler;


import com.example.kafkaproducerhomework.model.user.exception.CustomException;
import com.example.kafkaproducerhomework.model.user.exception.EmailConflictException;
import com.example.kafkaproducerhomework.model.user.exception.UserAuthenticationException;
import com.example.kafkaproducerhomework.model.user.exception.UserNotFoundException;
import com.example.kafkaproducerhomework.model.website.exception.WebsiteAlreadyExistsException;
import com.example.kafkaproducerhomework.model.website.exception.WebsiteNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(EmailConflictException.class)
    public ResponseEntity<String> handleEmailConflictException(EmailConflictException ex) {
        LOG.warn("Email conflict: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException ex) {
        LOG.warn("User not found: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(WebsiteAlreadyExistsException.class)
    public ResponseEntity<String> handleWebsiteAlreadyExistsException(WebsiteAlreadyExistsException ex) {
        LOG.warn("Website conflict: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(WebsiteNotFoundException.class)
    public ResponseEntity<String> handleWebsiteNotFoundException(WebsiteNotFoundException ex) {
        LOG.warn("Website not found: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(UserAuthenticationException.class)
    public ResponseEntity<String> handleUserAuthenticationException(UserAuthenticationException ex){
        LOG.warn("User not authenticated: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<String> handleCustomException(CustomException ex){
        LOG.debug("Not correct");
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
