package com.example.kafkaproducerhomework.controller.website;


import com.example.kafkaproducerhomework.model.user.AuthenticationCredentials;
import com.example.kafkaproducerhomework.model.website.Website;
import com.example.kafkaproducerhomework.model.website.request.CustomWebsiteCreateRequest;
import com.example.kafkaproducerhomework.model.website.request.SubWebsitesUpdateRequest;
import com.example.kafkaproducerhomework.model.website.response.WebsitesResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/default")
public interface WebsiteOperations {
    @GetMapping("/{id}")
    ResponseEntity<Website> get(@PathVariable Long id);

    @PostMapping("/custom")
    ResponseEntity<Website> create(@RequestBody CustomWebsiteCreateRequest customWebsiteCreateRequest);

    @DeleteMapping("/custom/{websiteId}")
    ResponseEntity<String> delete(@RequestBody AuthenticationCredentials credentials, @PathVariable Long websiteId);

    @GetMapping("/user")
    ResponseEntity<WebsitesResponse> getUsersWebsites(@RequestBody AuthenticationCredentials credentials);

    @PatchMapping
    ResponseEntity<String> patch(@RequestBody SubWebsitesUpdateRequest subWebsitesUpdateRequest);
}
