package com.example.kafkaconsumerhomework.cassandra;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.example.kafkaconsumerhomework.model.user.UserAuditInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UserAuditService {

    private static final Logger LOG = LoggerFactory.getLogger(UserAuditService.class);

    private final CqlSession cqlSession;

    private final ObjectMapper objectMapper;

    public UserAuditService(CqlSession cqlSession, ObjectMapper objectMapper) {
        this.cqlSession = cqlSession;
        this.objectMapper = objectMapper;
    }


    public boolean createRequest(UUID userId, Instant eventTime, String eventType, String eventDetails){
        try {
            PreparedStatement preparedStatement = cqlSession.prepare("INSERT INTO my_keyspace.user_audit" +
                    "(user_id, event_time, event_type, event_details) VALUES (?, ?, ?, ?)");

            BoundStatement boundStatement = preparedStatement.bind(userId, eventTime, eventType, eventDetails);
            cqlSession.execute(boundStatement);
            LOG.debug("Request executed");
            return true;
        } catch (Exception e){
            return false;
        }
    }

    public List<UserAuditInfo> getInfoByUserId(UUID userId){
        String query = "SELECT * FROM my_keyspace.user_audit WHERE user_id = ?";
        ResultSet rows = cqlSession.execute(query, userId);
        List<UserAuditInfo> userAuditInfos = new ArrayList<>();
        for (Row row : rows.all()) {
            UserAuditInfo userAuditInfo = new UserAuditInfo(row.getUuid("user_id"),
                    row.get("event_time", Instant.class),
                    row.getString("event_type"),
                    row.getString("event_details"));
            userAuditInfos.add(userAuditInfo);
        }
        LOG.debug("Info from request are got");
        return userAuditInfos;
    }

    @SneakyThrows
    @KafkaListener(topics = {"${topic-to-consume-message}"})
    public void consumeMessage(String message, Acknowledgment acknowledgment){
        try {
            UserAuditInfo userAuditInfo = objectMapper.readValue(message, UserAuditInfo.class);
            LOG.debug("Retrieved message {}", userAuditInfo);
            createRequest(userAuditInfo.userId(), userAuditInfo.eventTime(), userAuditInfo.eventType(), userAuditInfo.eventDetails());
            acknowledgment.acknowledge();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
