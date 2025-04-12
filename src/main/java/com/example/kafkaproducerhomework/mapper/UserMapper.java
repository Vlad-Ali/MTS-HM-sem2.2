package com.example.kafkaproducerhomework.mapper;



import com.example.kafkaproducerhomework.entity.UserEntity;
import com.example.kafkaproducerhomework.model.user.User;
import com.example.kafkaproducerhomework.model.user.UserId;

import java.util.UUID;

public class UserMapper {
    public static User toUser(UserEntity userEntity){
        UUID id = userEntity.getId();
        String email = userEntity.getEmail();
        String username = userEntity.getUsername();
        String password = userEntity.getPassword();
        return new User(new UserId(id), email, username, password);
    }

    public static UserEntity toUserEntity(User user){
        return new UserEntity(user.email(), user.password(), user.username());
    }
}
