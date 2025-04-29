package com.example.kafkaproducerhomework.model.user.request;


import com.example.kafkaproducerhomework.model.user.AuthenticationCredentials;

public record UserChangePasswordRequest(String email, String password, String newPassword) {
    public AuthenticationCredentials getCredentials(){
        return new AuthenticationCredentials(email, password);
    }
}
