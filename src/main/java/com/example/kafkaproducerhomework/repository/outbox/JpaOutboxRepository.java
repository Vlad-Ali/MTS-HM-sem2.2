package com.example.kafkaproducerhomework.repository.outbox;

import com.example.kafkaproducerhomework.entity.OutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaOutboxRepository extends JpaRepository<OutboxEntity, Long> {
}
