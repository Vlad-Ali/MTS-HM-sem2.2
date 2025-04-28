package com.example.kafkaproducerhomework.scheduler;

import com.example.kafkaproducerhomework.entity.OutboxEntity;
import com.example.kafkaproducerhomework.repository.outbox.JpaOutboxRepository;
import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class OutboxScheduler {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final NewTopic topic;
    private final JpaOutboxRepository outboxRepository;
    private final static Logger LOG = LoggerFactory.getLogger(OutboxScheduler.class);

    public OutboxScheduler(KafkaTemplate<String, String> kafkaTemplate, NewTopic topic, JpaOutboxRepository outboxRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
        this.outboxRepository = outboxRepository;
    }

    @Transactional
    @Scheduled(initialDelay = 10000, fixedRate = 10000 * 10)
    public void processOutbox() {
        processMessages(outboxRepository.findAll());
    }

    public void processMessages(List<OutboxEntity> messages) {
        for (OutboxEntity outboxEntity : messages) {
            CompletableFuture<SendResult<String, String>> sendResult = kafkaTemplate.send(topic.name(), outboxEntity.getData());
            handleKafkaResult(sendResult);
        }
        outboxRepository.deleteAll();
        LOG.debug("Outbox data add to topic");
    }

    void handleKafkaResult(CompletableFuture<SendResult<String, String>> future) {
        future.whenComplete((res, ex) -> {
            if (ex != null) {
                LOG.error("Failed to send message to Kafka", ex);
            }
        });
    }

}
