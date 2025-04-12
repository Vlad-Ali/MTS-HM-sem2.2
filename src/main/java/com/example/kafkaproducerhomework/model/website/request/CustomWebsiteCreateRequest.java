package com.example.kafkaproducerhomework.model.website.request;


import com.example.kafkaproducerhomework.model.user.AuthenticationCredentials;

public record CustomWebsiteCreateRequest(String email, String password, String url, String description) {
    public AuthenticationCredentials getCredentials(){
        return new AuthenticationCredentials(email, password);
    }
}
