package com.example.cassandrahomework.service.website;

import com.example.cassandrahomework.entity.UserEntity;
import com.example.cassandrahomework.entity.WebsiteEntity;
import com.example.cassandrahomework.mapper.WebsiteMapper;
import com.example.cassandrahomework.model.user.UserAuditInfo;
import com.example.cassandrahomework.model.user.UserId;
import com.example.cassandrahomework.model.user.exception.UserNotFoundException;
import com.example.cassandrahomework.model.website.Website;
import com.example.cassandrahomework.model.website.WebsiteId;
import com.example.cassandrahomework.model.website.exception.WebsiteAlreadyExistsException;
import com.example.cassandrahomework.model.website.exception.WebsiteNotFoundException;
import com.example.cassandrahomework.model.website.response.WebsitesResponse;
import com.example.cassandrahomework.repository.user.JpaUsersRepository;
import com.example.cassandrahomework.repository.website.JpaWebsitesRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class WebsitesService {
    private static final Logger LOG = LoggerFactory.getLogger(WebsitesService.class);
    private final JpaWebsitesRepository websitesRepository;
    private final JpaUsersRepository usersRepository;
    private final KafkaTemplate<String,String> kafkaTemplate;
    private final NewTopic topic;
    private final ObjectMapper objectMapper;


    public WebsitesService(JpaWebsitesRepository websitesRepository, JpaUsersRepository usersRepository, KafkaTemplate<String, String> kafkaTemplate, NewTopic topic, ObjectMapper objectMapper) {
        this.websitesRepository = websitesRepository;
        this.usersRepository = usersRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
        this.objectMapper = objectMapper;
    }

    @Cacheable(cacheNames = "Websites", key = "{#websiteId}")
    public Website findById(Long websiteId) {
        LOG.debug("Method findById called");
        Optional<WebsiteEntity> optionalWebsite = websitesRepository.findById(websiteId);
        if (optionalWebsite.isEmpty()){
            throw new WebsiteNotFoundException("Website is not found with id = " + websiteId);
        }
        WebsiteEntity websiteEntity = optionalWebsite.get();
        return WebsiteMapper.toWebsite(websiteEntity);
    }

    @Transactional(readOnly = true)
    public List<Website> getSubscribedWebsitesByUserId(UserId userId) {
        LOG.debug("Method getSubscribedWebsitesByUserId called");
        ArrayList<WebsiteEntity> websiteEntityArrayList = new ArrayList<>(websitesRepository.findSubscribedWebsitesByUserId(userId.getValue()));
        ArrayList<Website> websites = new ArrayList<>();
        for(WebsiteEntity entity : websiteEntityArrayList){
            websites.add(WebsiteMapper.toWebsite(entity));
        }
        return websites.stream().toList();
    }

    @Transactional(readOnly = true)
    public List<Website> getUnSubscribedWebsitesByUserId(UserId userId) {
        LOG.debug("Method getUnSubscribedWebsitesByUserId called");
        ArrayList<WebsiteEntity> websiteEntityArrayList = new ArrayList<>(websitesRepository.findUnSubscribedWebsitesByUserId(userId.getValue()));
        ArrayList<Website> websites = new ArrayList<>();
        for(WebsiteEntity entity : websiteEntityArrayList){
            websites.add(WebsiteMapper.toWebsite(entity));
        }
        return websites.stream().toList();
    }

    @Transactional(readOnly = true)
    public WebsitesResponse getSubAndUnSubWebsites(UserId userId){
        LOG.debug("Method getSubAndUnSubWebsites called");
        List<Website> subWebsites = getSubscribedWebsitesByUserId(userId);
        List<Website> unSubWebsites = getUnSubscribedWebsitesByUserId(userId);
        try {
            String message = objectMapper.writeValueAsString(new UserAuditInfo(userId.getValue(), Instant.now(), WebsitesAction.SELECT.name(), "User got his subWebsites"));
            CompletableFuture<SendResult<String,String>> sendResult = kafkaTemplate.send(topic.name(), message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return new WebsitesResponse(getSubscribedWebsitesByUserId(userId), getUnSubscribedWebsitesByUserId(userId));
    }

    @Transactional
    public Website create(Website website) {
        LOG.debug("Method create called");
        Optional<WebsiteEntity> optionalWebsite = websitesRepository.findByUrl(website.url());
        if (optionalWebsite.isPresent()){
            throw new WebsiteAlreadyExistsException("Website already exists with url = " + website.url());
        }
        Optional<UserEntity> optionalUser = usersRepository.findById(website.creatorId().getValue());
        UserEntity userEntity = optionalUser.orElseThrow(() -> new UserNotFoundException("User not found with id = "+website.creatorId().getValue()));
        WebsiteEntity websiteEntity = WebsiteMapper.toWebsiteEntity(website, userEntity);
        userEntity.addWebsite(websiteEntity);
        UserEntity savedUser = usersRepository.save(userEntity);
        WebsiteEntity savedWebsite = savedUser.getCreatedWebsites().stream()
                .filter(web -> web.getUrl().equals(website.url()))
                .findFirst().get();
        try {
            String message = objectMapper.writeValueAsString(new UserAuditInfo(website.creatorId().getValue(), Instant.now(), WebsitesAction.INSERT.name(), "User created website"));
            CompletableFuture<SendResult<String,String>> sendResult = kafkaTemplate.send(topic.name(), message);
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return WebsiteMapper.toWebsite(savedWebsite);
    }

    @Transactional
    public void delete(WebsiteId websiteId, UserId userId) {
        LOG.debug("Method delete called");
        Optional<UserEntity> optionalUser = usersRepository.findById(userId.getValue());
        UserEntity userEntity = optionalUser.get();
        Optional<WebsiteEntity> optionalWebsite = websitesRepository.findById(websiteId.getValue());
        if(optionalWebsite.isEmpty()){
            throw new WebsiteNotFoundException("Website is not found with id = " + websiteId.getValue());
        }
        WebsiteEntity websiteEntity = optionalWebsite.get();
        if (websiteEntity.getCreatorId()==null || !websiteEntity.getCreatorId().equals(userEntity.getId())){
            throw new WebsiteNotFoundException("Website is not found with id = " + websiteId.getValue());
        }
        userEntity.removeWebsite(websiteEntity);
        usersRepository.save(userEntity);
        websitesRepository.deleteById(websiteId.getValue());
        try {
            String message = objectMapper.writeValueAsString(new UserAuditInfo(userId.getValue(), Instant.now(), WebsitesAction.DELETE.name(), "User deleted his website with id = "+websiteId.getValue()));
            CompletableFuture<SendResult<String, String>> sendResult = kafkaTemplate.send(topic.name(), message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public void updateSubWebsites(List<WebsiteId> websites, UserId userId){
        LOG.debug("Method updateSubWebsites called");
        ArrayList<WebsiteEntity> websiteEntityArrayList = new ArrayList<>();
        for (WebsiteId websiteId : websites){
            Optional<WebsiteEntity> optionalWebsite = websitesRepository.findById(websiteId.getValue());
            if (optionalWebsite.isEmpty()){
                throw new WebsiteNotFoundException("Website is not found with id = " + websiteId.getValue());
            }
            websiteEntityArrayList.add(optionalWebsite.get());
        }
        UserEntity userEntity = usersRepository.findById(userId.getValue()).get();
        userEntity.getSubscribedWebsites().clear();
        for (WebsiteEntity websiteEntity : websiteEntityArrayList){
            userEntity.subscribeToWebsite(websiteEntity);
        }
        usersRepository.save(userEntity);
        try {
            String message = objectMapper.writeValueAsString(new UserAuditInfo(userId.getValue(), Instant.now(), WebsitesAction.UPDATE.name(), "User updated his sub websites"));
            CompletableFuture<SendResult<String, String>> sendResult = kafkaTemplate.send(topic.name(), message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
