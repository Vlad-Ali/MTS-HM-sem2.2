package com.example.kafkaproducerhomework.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "outbox")
@Getter
@Setter
public class OutboxEntity {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @NonNull
    @Column(name = "data")
    private String data;

    protected OutboxEntity() {}

    public OutboxEntity(@NonNull String data){
        this.id = null;
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OutboxEntity outboxEntity)) {
            return false;
        }
        return id != null && id.equals(outboxEntity.id);
    }

    @Override
    public int hashCode() {
        return OutboxEntity.class.hashCode();
    }

    public @NonNull String getData() {
        return data;
    }
}
