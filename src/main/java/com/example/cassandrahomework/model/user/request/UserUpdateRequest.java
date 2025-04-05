package com.example.cassandrahomework.model.user.request;

import com.example.cassandrahomework.model.user.AuthenticationCredentials;
import com.example.cassandrahomework.model.user.UserInfo;

public record UserUpdateRequest(String email, String password, String newEmail, String newUsername) {
    public AuthenticationCredentials getCredentials(){
    return new AuthenticationCredentials(email, password);
    }
    public UserInfo getNewUserInfo(){
        return new UserInfo(newEmail, newUsername);
    }
}
