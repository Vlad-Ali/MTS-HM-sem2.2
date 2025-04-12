package com.example.kafkaproducerhomework.model.user;

import java.util.Objects;
import java.util.UUID;

public class UserId {
    private final UUID value;

    public UserId(UUID value) {
        this.value = value;
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o){
        if (this == o){
            return true;
        }
        if (o instanceof UserId userId){
            return value.equals(userId.value);
        }
        return false;
    }
    @Override
    public int hashCode(){return Objects.hash(value);}
}
