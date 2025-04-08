package databasesuite;

import com.datastax.oss.driver.api.core.CqlSession;
import com.example.cassandrahomework.cassandra.UserAuditService;
import com.example.cassandrahomework.objectmapper.ObjectMapperConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.lifecycle.Startables;

import java.net.InetSocketAddress;

@ContextConfiguration(initializers = DatabaseSuite.Initializer.class)
public class DatabaseSuite {
    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16");

    @Container
    private static final CassandraContainer<?> CASSANDRA = new CassandraContainer<>("cassandra:4.1");

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext context) {
            Startables.deepStart(POSTGRES).join();

            TestPropertyValues.of(
                    "spring.datasource.url=" + POSTGRES.getJdbcUrl(),
                    "spring.datasource.username=" + POSTGRES.getUsername(),
                    "spring.datasource.password=" + POSTGRES.getPassword()
            ).applyTo(context);

            Startables.deepStart(CASSANDRA).join();

            TestPropertyValues.of(
                    "cassandra.contact-points="+CASSANDRA.getHost(),
                    "cassandra.port="+CASSANDRA.getContactPoint().getPort(),
                    "cassandra.local-datacenter=" + "datacenter1"
            ).applyTo(context);
        }
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
        public ObjectMapper objectMapper(){
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            return objectMapper;
        }

        @Bean
        public UserAuditService userAuditService(CqlSession cqlSession, ObjectMapper objectMapper) {
            return new UserAuditService(cqlSession, objectMapper);
        }

    }
}
