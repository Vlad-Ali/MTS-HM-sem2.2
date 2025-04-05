package com.example.cassandrahomework.model.website.exception;

public class WebsiteAlreadyExistsException extends RuntimeException {
    public WebsiteAlreadyExistsException(String message) {
        super(message);
    }
}
