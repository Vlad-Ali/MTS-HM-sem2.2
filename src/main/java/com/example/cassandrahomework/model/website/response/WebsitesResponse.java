package com.example.cassandrahomework.model.website.response;

import com.example.cassandrahomework.model.website.Website;

import java.util.List;

public record WebsitesResponse(List<Website> subscribed, List<Website> other) {
}
