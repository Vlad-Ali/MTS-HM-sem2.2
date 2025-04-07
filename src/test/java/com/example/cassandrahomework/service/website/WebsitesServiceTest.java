package com.example.cassandrahomework.service.website;

import com.datastax.oss.driver.api.core.CqlSession;
import com.example.cassandrahomework.model.user.User;
import com.example.cassandrahomework.model.user.UserAuditInfo;
import com.example.cassandrahomework.model.user.UserId;
import com.example.cassandrahomework.model.user.exception.UserNotFoundException;
import com.example.cassandrahomework.model.website.Website;
import com.example.cassandrahomework.model.website.WebsiteId;
import com.example.cassandrahomework.repository.user.JpaUsersRepository;
import com.example.cassandrahomework.repository.website.JpaWebsitesRepository;
import com.example.cassandrahomework.service.user.UsersService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import databasesuite.DatabaseSuite;
import jakarta.persistence.EntityManager;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;


import java.net.InetSocketAddress;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(
properties = {"topic-to-send-message=test-topic1"})
@Import({KafkaAutoConfiguration.class})
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class WebsitesServiceTest extends DatabaseSuite{

    @Container
    private static final CassandraContainer<?> CASSANDRA = new CassandraContainer<>("cassandra:4.1").withExposedPorts(9042);

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

    @DynamicPropertySource
    static void cassandraProperties(DynamicPropertyRegistry registry) {
        registry.add("cassandra.contact-points", CASSANDRA::getHost);
        registry.add("cassandra.port", () -> CASSANDRA.getMappedPort(9042));
        registry.add("cassandra.local-datacenter", () -> "datacenter1");
    }

    @BeforeAll
    static void setCassandra(){
        try (CqlSession session = CqlSession.builder()
                .addContactPoint(new InetSocketAddress(CASSANDRA.getHost(), CASSANDRA.getMappedPort(9042)))
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
    void shouldSendMessageToKafkaSuccessfully() {
        User user = new User(new UserId(null), "1", "1", "1");
        User createdUser = usersService.register(user);
        Website website = new Website(new WebsiteId(null), "1", "1", createdUser.id());
        assertDoesNotThrow(() -> websitesService.create(website));
        KafkaTestConsumer consumer = new KafkaTestConsumer(KAFKA.getBootstrapServers(), "some-group-id-"+UUID.randomUUID());
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
        KafkaTestConsumer consumer = new KafkaTestConsumer(KAFKA.getBootstrapServers(), "some-group-id-" + UUID.randomUUID());
        consumer.subscribe(List.of(testTopic.name()));
        ConsumerRecords<String, String> records = consumer.poll();
        assertEquals(0, records.count());
    }



}


