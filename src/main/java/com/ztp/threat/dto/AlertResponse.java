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
    private final String correlationId;
    private final String outcome;
    private final java.util.List<com.ztp.risk.ScoreReason> reasons;

    private static final com.fasterxml.jackson.databind.ObjectMapper OBJECT_MAPPER = new com.fasterxml.jackson.databind.ObjectMapper();

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
        this.correlationId = a.getCorrelationId();
        this.outcome = a.getOutcome();
        this.reasons = parseReasons(a.getReasonsJson());
    }

    private static java.util.List<com.ztp.risk.ScoreReason> parseReasons(String json) {
        if (json == null || json.isBlank()) return java.util.List.of();
        try {
            return OBJECT_MAPPER.readValue(json,
                    OBJECT_MAPPER.getTypeFactory().constructCollectionType(java.util.List.class, com.ztp.risk.ScoreReason.class));
        } catch (Exception e) {
            return java.util.List.of();
        }
    }
}