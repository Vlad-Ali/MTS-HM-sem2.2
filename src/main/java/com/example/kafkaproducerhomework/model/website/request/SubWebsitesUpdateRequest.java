package com.example.kafkaproducerhomework.model.website.request;


import com.example.kafkaproducerhomework.model.user.AuthenticationCredentials;

import java.util.List;

public record SubWebsitesUpdateRequest(String email, String password, List<Long> websiteIds) {
    public AuthenticationCredentials getCredentials(){
        return new AuthenticationCredentials(email, password);
    }
}
