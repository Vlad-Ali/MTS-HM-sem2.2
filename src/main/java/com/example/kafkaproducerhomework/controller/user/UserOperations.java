package com.example.kafkaproducerhomework.controller.user;

import com.example.kafkaproducerhomework.model.user.AuthenticationCredentials;
import com.example.kafkaproducerhomework.model.user.User;
import com.example.kafkaproducerhomework.model.user.UserInfo;
import com.example.kafkaproducerhomework.model.user.request.UserChangePasswordRequest;
import com.example.kafkaproducerhomework.model.user.request.UserRegisterRequest;
import com.example.kafkaproducerhomework.model.user.request.UserUpdateRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;


@RequestMapping("/default")
public interface UserOperations {

    @PostMapping("/register")
    ResponseEntity<User> register(@RequestBody UserRegisterRequest userRegisterRequest);

    @PatchMapping
    ResponseEntity<String> update(@RequestBody UserUpdateRequest userUpdateRequest);

    @GetMapping
    ResponseEntity<UserInfo> get(@RequestBody AuthenticationCredentials credentials);

    @PutMapping("/password")
    ResponseEntity<String> changePassword(@RequestBody UserChangePasswordRequest userChangePasswordRequest);
}
