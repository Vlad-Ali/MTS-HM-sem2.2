package com.example.kafkaproducerhomework.service.website;

import com.example.kafkaproducerhomework.model.user.User;
import com.example.kafkaproducerhomework.model.user.UserAuditInfo;
import com.example.kafkaproducerhomework.model.user.UserId;
import com.example.kafkaproducerhomework.model.user.exception.UserNotFoundException;
import com.example.kafkaproducerhomework.model.website.Website;
import com.example.kafkaproducerhomework.model.website.WebsiteId;
import com.example.kafkaproducerhomework.scheduler.OutboxScheduler;
import com.example.kafkaproducerhomework.service.user.UsersService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        properties = {"topic-to-send-message=test-topic1"})
@Import({KafkaAutoConfiguration.class})
@Transactional
@Testcontainers
public class WebsitesServiceTest{

    @ServiceConnection
    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16");

    @Container
    @ServiceConnection
    public static final KafkaContainer KAFKA = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

    @Autowired
    private UsersService usersService;

    @Autowired
    private WebsitesService websitesService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NewTopic testTopic;

    @Autowired
    private OutboxScheduler outboxScheduler;

    @Test
    void shouldSendMessageToKafkaSuccessfully() {
        User user = new User(new UserId(null), "1", "1", "1");
        User createdUser = usersService.register(user);
        Website website = new Website(new WebsiteId(null), "1", "1", createdUser.id());
        assertDoesNotThrow(() -> websitesService.create(website));
        KafkaTestConsumer consumer = new KafkaTestConsumer(KAFKA.getBootstrapServers(), "some-group-id-"+ UUID.randomUUID());
        outboxScheduler.processOutbox();
        consumer.subscribe(List.of(testTopic.name()));
        ConsumerRecords<String, String> records = consumer.poll();
        assertEquals(1, records.count());
        records.iterator().forEachRemaining(
                record -> {
                    try {
                        UserAuditInfo userAuditInfo = objectMapper.readValue(record.value(), UserAuditInfo.class);
                        assertEquals(userAuditInfo.userId(), createdUser.id().getValue());
                        assertEquals(userAuditInfo.eventType(), WebsitesAction.INSERT.name());
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    @Test
    void shouldNotSendMessageToKafka(){
        Website website = new Website(new WebsiteId(null), "1", "1", new UserId(UUID.randomUUID()));
        assertThrows(UserNotFoundException.class, ()->websitesService.create(website));
        outboxScheduler.processOutbox();
        KafkaTestConsumer consumer = new KafkaTestConsumer(KAFKA.getBootstrapServers(), "some-group-id-" + UUID.randomUUID());
        consumer.subscribe(List.of(testTopic.name()));
        ConsumerRecords<String, String> records = consumer.poll();
        assertEquals(0, records.count());

    }
}