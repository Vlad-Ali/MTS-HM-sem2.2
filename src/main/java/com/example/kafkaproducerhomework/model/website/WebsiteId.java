package com.example.kafkaproducerhomework.model.website;

import java.util.Objects;

public class WebsiteId {
    private final Long value;

    public WebsiteId(Long value) {
        this.value = value;
    }

    public Long getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o instanceof WebsiteId websiteId) {
            return value.equals(websiteId.value);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
