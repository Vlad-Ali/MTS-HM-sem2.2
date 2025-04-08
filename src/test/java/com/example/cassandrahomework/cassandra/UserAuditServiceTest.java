package com.example.cassandrahomework.cassandra;

import com.datastax.oss.driver.api.core.CqlSession;
import com.example.cassandrahomework.kafka.KafkaTopicConfig;
import com.example.cassandrahomework.model.user.UserAuditInfo;
import com.example.cassandrahomework.objectmapper.ObjectMapperConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import databasesuite.DatabaseSuite;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(properties = {"topic-to-consume-message=test-topic",
        "spring.kafka.consumer.group-id=some-consumer-group",
        "topic-to-send-message=test-topic"
})
@Testcontainers
@Import({KafkaAutoConfiguration.class, ObjectMapperConfig.class, KafkaTopicConfig.class})
public class UserAuditServiceTest{

    @Container
    @ServiceConnection
    public static final KafkaContainer KAFKA = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

    @Container
    private static final CassandraContainer<?> CASSANDRA = new CassandraContainer<>("cassandra:4.1");

    @Autowired
    private UserAuditService userAuditService;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NewTopic topic;

    @DynamicPropertySource
    static void cassandraProperties(DynamicPropertyRegistry registry) {
        registry.add("cassandra.contact-points", CASSANDRA::getHost);
        registry.add("cassandra.port", () -> CASSANDRA.getContactPoint().getPort());
        registry.add("cassandra.local-datacenter", () -> "datacenter1");
    }

    @BeforeAll
    static void setCassandra(){
        try (CqlSession session = CqlSession.builder()
                .addContactPoint(new InetSocketAddress(CASSANDRA.getHost(), CASSANDRA.getContactPoint().getPort()))
                .withLocalDatacenter("datacenter1")
                .build()) {

            session.execute("CREATE KEYSPACE IF NOT EXISTS my_keyspace WITH replication = "
                    + "{'class':'SimpleStrategy', 'replication_factor':1};");

            session.execute("CREATE TABLE IF NOT EXISTS my_keyspace.user_audit ("
                    + "user_id UUID,"
                    + "event_time TIMESTAMP,"
                    + "event_type TEXT,"
                    + "event_details TEXT,"
                    + "PRIMARY KEY ((user_id), event_time)"
                    + ") WITH CLUSTERING ORDER BY (event_time DESC);");
        }
    }

    @Test
    void shouldCreateRequest(){
        UUID userId = UUID.randomUUID();
        Instant eventTime = Instant.now();
        String eventType = "SELECT";
        String eventDetails = "Sub websites are got";
        UserAuditInfo userAuditInfo = new UserAuditInfo(userId, eventTime, eventType, eventDetails);
        try {
            kafkaTemplate.send(topic.name(), objectMapper.writeValueAsString(userAuditInfo));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        await().atMost(Duration.ofSeconds(5)).pollDelay(Duration.ofSeconds(1))
                .untilAsserted(()->{
                    List<UserAuditInfo> infos = userAuditService.getInfoByUserId(userId);
                    assertEquals(userAuditInfo.userId(), infos.get(0).userId());
                    assertEquals(userAuditInfo.eventType(), infos.get(0).eventType());
                });
        //assertTrue(userAuditService.createRequest(userId, eventTime, eventType, eventDetails));
    }

    @Test
    void shouldNotCreateRequest(){
        UUID userId = UUID.randomUUID();
        Instant eventTime = Instant.now();
        String eventType = "SELECT";
        String eventDetails = "Sub websites are got";
        userAuditService.createRequest(userId, eventTime, eventType, eventDetails);
        assertFalse(userAuditService.createRequest(null, eventTime, eventType, eventDetails));
    }

    @Test
    void shouldCreateRequestAndGetAuditInfo(){
        UUID userId = UUID.randomUUID();
        Instant eventTime = Instant.now();
        String eventType = "SELECT";
        String eventDetails = "Sub websites are got";

        userAuditService.createRequest(userId, eventTime, eventType, eventDetails);

        List<UserAuditInfo> auditInfoList = userAuditService.getInfoByUserId(userId);

        UserAuditInfo userAuditInfo = auditInfoList.get(0);
        Assertions.assertEquals(userId, userAuditInfo.userId());
        Assertions.assertEquals(eventTime.toEpochMilli(), userAuditInfo.eventTime().toEpochMilli());
        Assertions.assertEquals(eventType, userAuditInfo.eventType());
        Assertions.assertEquals(eventDetails, userAuditInfo.eventDetails());

    }


    @Configuration
    static class TestConfig {
        @Bean
        public CqlSession cqlSession() {
            return CqlSession.builder()
                    .addContactPoint(new InetSocketAddress(CASSANDRA.getHost(), CASSANDRA.getContactPoint().getPort()))
                    .withLocalDatacenter("datacenter1")
                    .build();
        }

        @Bean
        public UserAuditService userAuditService(CqlSession cqlSession,  ObjectMapper objectMapper) {
            return new UserAuditService(cqlSession, objectMapper);
        }

    }

}