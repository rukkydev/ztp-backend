package com.ztp.threat.dto;

import com.ztp.threat.Alert;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class AlertResponse {
    private final Long id;
    private final String title;
    private final String description;
    private final String severity;
    private final String status;
    private final Long userId;
    private final String username;
    private final LocalDateTime createdAt;
    private final LocalDateTime resolvedAt;

    public AlertResponse(Alert a) {
        this.id = a.getId();
        this.title = a.getTitle();
        this.description = a.getDescription();
        this.severity = a.getSeverity();
        this.status = a.getStatus();
        this.userId = a.getUserId();
        this.username = a.getUsername();
        this.createdAt = a.getCreatedAt();
        this.resolvedAt = a.getResolvedAt();
    }
}