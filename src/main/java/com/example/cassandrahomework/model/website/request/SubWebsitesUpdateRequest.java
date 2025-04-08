package com.example.cassandrahomework.model.website.request;

import com.example.cassandrahomework.model.user.AuthenticationCredentials;

import java.util.List;

public record SubWebsitesUpdateRequest(String email, String password, List<Long> websiteIds) {
    public AuthenticationCredentials getCredentials(){
        return new AuthenticationCredentials(email, password);
    }
}
