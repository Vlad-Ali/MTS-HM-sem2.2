package com.example.kafkaproducerhomework.model.website;


import com.example.kafkaproducerhomework.model.user.User;
import com.example.kafkaproducerhomework.model.user.UserId;

public record Website(WebsiteId id, String url, String description, UserId creatorId) {
    public static final Website WEBSITE_1 = new Website(new WebsiteId(1L), "1", "1", new UserId(User.USER_1.id().getValue()));
    public static final Website WEBSITE_2 = new Website(new WebsiteId(2L), "2", "2", new UserId(User.USER_2.id().getValue()));
    public static final Website WEBSITE_3 = new Website(new WebsiteId(3L), "3", "3", new UserId(User.USER_3.id().getValue()));

    public Website initializeWithId(WebsiteId newId) {
        return new Website(newId, url, description, creatorId);
    }

    public Website withUrl(String newUrl) {
        return new Website(id, newUrl, description, creatorId);
    }

    public Website withDescription(String newDescription) {
        return new Website(id, url, newDescription, creatorId);
    }

    public Website withCreatorId(UserId newCreatorId) {
        return new Website(id, url, description, newCreatorId);
    }

}
