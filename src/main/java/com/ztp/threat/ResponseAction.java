package com.ztp.threat;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "response_actions", indexes = {
        @Index(name = "idx_response_action_correlation", columnList = "correlation_id"),
        @Index(name = "idx_response_action_created", columnList = "executed_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "correlation_id", length = 64)
    private String correlationId;

    @Column(name = "action_type", nullable = false, length = 50)
    private String actionType; // e.g. "BLOCK_LOGIN", "BLOCK_DEVICE", "FORCE_CHALLENGE", "TERMINATE_SESSIONS"

    @Column(nullable = false, length = 20)
    private String status; // "EXECUTED" | "FAILED"

    @Column(length = 500)
    private String details;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "target_entity", length = 100)
    private String targetEntity; // e.g. IP address, deviceId, username

    @Column(name = "executed_at", nullable = false, updatable = false)
    private LocalDateTime executedAt;

    @PrePersist
    protected void onCreate() {
        this.executedAt = LocalDateTime.now();
    }
}
