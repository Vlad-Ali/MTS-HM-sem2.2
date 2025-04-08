package com.example.kafkaconsumerhomework.controller;

import com.example.kafkaconsumerhomework.cassandra.UserAuditService;
import com.example.kafkaconsumerhomework.model.user.UserAuditInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/api/user")
public class UserAuditController {
    private static final Logger LOG = LoggerFactory.getLogger(UserAuditController.class);
    private final UserAuditService userAuditService;

    public UserAuditController(UserAuditService userAuditService) {
        this.userAuditService = userAuditService;
    }

    @RequestMapping("/audit")
    public ResponseEntity<List<UserAuditInfo>> getAuditInfo(@RequestParam UUID userId){
        List<UserAuditInfo> userAuditInfos = userAuditService.getInfoByUserId(userId);
        LOG.debug("AuditInfo is got");
        return ResponseEntity.ok(userAuditInfos);
    }

}
