package com.example.kafkaproducerhomework.model.user;

import java.util.UUID;

public record User(UserId id, String email, String password, String username) {
    public static final User USER_1 = new User(new UserId(UUID.fromString("11111111-1111-1111-1111-111111111111")),"1","1","1");
    public static final User USER_2 = new User(new UserId(UUID.fromString("22222222-2222-2222-2222-222222222222")), "2", "2", "2");
    public static final User USER_3 = new User(new UserId(UUID.fromString("33333333-3333-3333-3333-333333333333")), "3", "3", "3");
    public User withId(final UserId newId){
         return new User(newId, email, password, username);
     }
     public User withEmail(final String newEmail){
         return new User(id, newEmail, password, username);
     }
     public User withPassword(final String newPassword){
         return new User(id, email, newPassword, username);
     }
     public User withUsername(final String newUsername){
         return new User(id, email, password, newUsername);
     }
}
