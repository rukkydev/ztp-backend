package com.ztp.threat.dto;

import com.ztp.threat.Threat;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class ThreatResponse {
    private final Long id;
    private final String title;
    private final String description;
    private final String severity;
    private final String status;
    private final Long userId;
    private final String username;
    private final LocalDateTime createdAt;
    private final LocalDateTime mitigatedAt;

    public ThreatResponse(Threat t) {
        this.id = t.getId();
        this.title = t.getTitle();
        this.description = t.getDescription();
        this.severity = t.getSeverity();
        this.status = t.getStatus();
        this.userId = t.getUserId();
        this.username = t.getUsername();
        this.createdAt = t.getCreatedAt();
        this.mitigatedAt = t.getMitigatedAt();
    }
}