package com.example.cassandrahomework.model.website.request;

import com.example.cassandrahomework.model.user.AuthenticationCredentials;

public record CustomWebsiteCreateRequest(String email, String password, String url, String description) {
    public AuthenticationCredentials getCredentials(){
        return new AuthenticationCredentials(email, password);
    }
}
