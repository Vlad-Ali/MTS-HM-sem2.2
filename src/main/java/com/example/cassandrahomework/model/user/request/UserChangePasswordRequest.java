package com.example.cassandrahomework.model.user.request;

import com.example.cassandrahomework.model.user.AuthenticationCredentials;

public record UserChangePasswordRequest(String email, String password, String newPassword) {
    public AuthenticationCredentials getCredentials(){
        return new AuthenticationCredentials(email, password);
    }
}
