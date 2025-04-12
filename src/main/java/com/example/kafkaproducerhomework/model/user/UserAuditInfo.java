package com.example.kafkaproducerhomework.model.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record UserAuditInfo(UUID userId, Instant eventTime, String eventType, String eventDetails) {
    @JsonCreator
    public UserAuditInfo(@JsonProperty("userId") UUID userId, @JsonProperty("eventTime") Instant eventTime, @JsonProperty("eventType") String eventType, @JsonProperty("eventDetails") String eventDetails){
        this.userId = userId;
        this.eventTime = eventTime;
        this.eventType = eventType;
        this.eventDetails = eventDetails;
    }

}
