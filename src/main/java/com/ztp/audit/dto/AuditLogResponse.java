package com.ztp.audit.dto;

import com.ztp.audit.AuditLog;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AuditLogResponse {
    private final Long id;
    private final String eventType;
    private final Long actorUserId;
    private final String actorUsername;
    private final String description;
    private final String ipAddress;
    private final LocalDateTime createdAt;

    public AuditLogResponse(AuditLog log) {
        this.id = log.getId();
        this.eventType = log.getEventType();
        this.actorUserId = log.getActorUserId();
        this.actorUsername = log.getActorUsername();
        this.description = log.getDescription();
        this.ipAddress = log.getIpAddress();
        this.createdAt = log.getCreatedAt();
    }
}