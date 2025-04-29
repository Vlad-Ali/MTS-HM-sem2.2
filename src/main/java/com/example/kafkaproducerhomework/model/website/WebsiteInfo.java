package com.example.kafkaproducerhomework.model.website;

import java.util.UUID;

public record WebsiteInfo(String url, String description, UUID userId) {
}
