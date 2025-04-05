package com.example.cassandrahomework.mapper;

import com.example.cassandrahomework.entity.UserEntity;
import com.example.cassandrahomework.entity.WebsiteEntity;
import com.example.cassandrahomework.model.user.UserId;
import com.example.cassandrahomework.model.website.Website;
import com.example.cassandrahomework.model.website.WebsiteId;

import java.util.UUID;

public class WebsiteMapper {
    public static Website toWebsite(WebsiteEntity websiteEntity){
        Long websiteId = websiteEntity.getWebsiteId();
        String url = websiteEntity.getUrl();
        String description = websiteEntity.getDescription();
        UUID userId = websiteEntity.getCreatorId();
        return new Website(new WebsiteId(websiteId), url, description, new UserId(userId));
    }

    public static WebsiteEntity toWebsiteEntity(Website website, UserEntity userEntity){
        String url = website.url();
        String description = website.description();
        return new WebsiteEntity(url, description, userEntity);
    }
}
