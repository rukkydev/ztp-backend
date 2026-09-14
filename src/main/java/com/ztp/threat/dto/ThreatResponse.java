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
    private final String correlationId;
    private final String outcome;
    private final java.util.List<com.ztp.risk.ScoreReason> reasons;

    private static final com.fasterxml.jackson.databind.ObjectMapper OBJECT_MAPPER = new com.fasterxml.jackson.databind.ObjectMapper();

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
        this.correlationId = t.getCorrelationId();
        this.outcome = t.getOutcome();
        this.reasons = parseReasons(t.getReasonsJson());
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