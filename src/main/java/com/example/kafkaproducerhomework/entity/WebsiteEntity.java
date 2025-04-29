package com.example.kafkaproducerhomework.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NonNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Table(name = "websites", uniqueConstraints = {@UniqueConstraint(columnNames = {"url"})})
public class WebsiteEntity {
    @Id
    @Column(name = "website_id", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long websiteId;

    @Column(name = "url")
    @NonNull
    private String url;
    @Column(name = "description")
    @NonNull
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "creator_id", nullable = true)
    private UserEntity creator;

    @ManyToMany(mappedBy = "subscribedWebsites")
    private Set<UserEntity> subscribers = new HashSet<>();

    protected WebsiteEntity(){}

    public WebsiteEntity(String url, String description, UserEntity creator){
        this.creator = creator;
        this.url = url;
        this.description = description;
    }

    public @NonNull String getUrl() {
        return url;
    }

    public void setUrl(@NonNull String url) {
        this.url = url;
    }

    public @NonNull String getDescription() {
        return description;
    }

    public Long getWebsiteId() {
        return websiteId;
    }

    public void setDescription(@NonNull String description) {
        this.description = description;
    }

    public UUID getCreatorId() {
        if (creator == null) return null;
        return creator.getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WebsiteEntity website)) {
            return false;
        }
        return websiteId != null && websiteId.equals(website.websiteId);
    }

    @Override
    public int hashCode() {
        return WebsiteEntity.class.hashCode();
    }

    public UserEntity getCreator() {
        return creator;
    }

    public void setCreator(UserEntity creator) {
        this.creator = creator;
    }

    public Set<UserEntity> getSubscribers() {
        return subscribers;
    }
}
