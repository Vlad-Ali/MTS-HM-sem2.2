package com.example.kafkaproducerhomework.service.website;

import com.example.kafkaproducerhomework.entity.OutboxEntity;
import com.example.kafkaproducerhomework.entity.UserEntity;
import com.example.kafkaproducerhomework.entity.WebsiteEntity;
import com.example.kafkaproducerhomework.mapper.WebsiteMapper;
import com.example.kafkaproducerhomework.model.user.UserAuditInfo;
import com.example.kafkaproducerhomework.model.user.UserId;
import com.example.kafkaproducerhomework.model.user.exception.UserNotFoundException;
import com.example.kafkaproducerhomework.model.website.Website;
import com.example.kafkaproducerhomework.model.website.WebsiteId;
import com.example.kafkaproducerhomework.model.website.exception.WebsiteAlreadyExistsException;
import com.example.kafkaproducerhomework.model.website.exception.WebsiteNotFoundException;
import com.example.kafkaproducerhomework.model.website.response.WebsitesResponse;
import com.example.kafkaproducerhomework.repository.outbox.JpaOutboxRepository;
import com.example.kafkaproducerhomework.repository.user.JpaUsersRepository;
import com.example.kafkaproducerhomework.repository.website.JpaWebsitesRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class WebsitesService {
    private static final Logger LOG = LoggerFactory.getLogger(WebsitesService.class);
    private final JpaWebsitesRepository websitesRepository;
    private final JpaUsersRepository usersRepository;
    private final ObjectMapper objectMapper;
    private final JpaOutboxRepository outboxRepository;


    public WebsitesService(JpaWebsitesRepository websitesRepository, JpaUsersRepository usersRepository, ObjectMapper objectMapper, JpaOutboxRepository outboxRepository) {
        this.websitesRepository = websitesRepository;
        this.usersRepository = usersRepository;
        this.objectMapper = objectMapper;
        this.outboxRepository = outboxRepository;
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

    @Transactional()
    public WebsitesResponse getSubAndUnSubWebsites(UserId userId){
        LOG.debug("Method getSubAndUnSubWebsites called");
        List<Website> subWebsites = getSubscribedWebsitesByUserId(userId);
        List<Website> unSubWebsites = getUnSubscribedWebsitesByUserId(userId);
        try {
            String message = objectMapper.writeValueAsString(new UserAuditInfo(userId.getValue(), Instant.now(), WebsitesAction.SELECT.name(), "User got his subWebsites"));
            outboxRepository.save(new OutboxEntity(message));
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
            outboxRepository.save(new OutboxEntity(message));
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
            outboxRepository.save(new OutboxEntity(message));
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
            outboxRepository.save(new OutboxEntity(message));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
