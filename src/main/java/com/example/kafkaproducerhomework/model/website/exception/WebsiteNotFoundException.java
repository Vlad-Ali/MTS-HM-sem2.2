package com.example.kafkaproducerhomework.model.website.exception;

public class WebsiteNotFoundException extends RuntimeException {
    public WebsiteNotFoundException(String message) {
        super(message);
    }
}
