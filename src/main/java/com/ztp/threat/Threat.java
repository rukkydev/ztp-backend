package com.ztp.threat;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "threats")
@Getter
@Setter
@NoArgsConstructor
public class Threat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, length = 20)
    private String severity; // High | Critical

    @Column(nullable = false, length = 20)
    private String status = "Active"; // Active | Mitigated

    @Column(name = "user_id")
    private Long userId;

    @Column(length = 100)
    private String username;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "mitigated_at")
    private LocalDateTime mitigatedAt;

    @Column(name = "correlation_id", length = 64)
    private String correlationId;

    @Column(name = "outcome", length = 30)
    private String outcome; // CONFIRMED_THREAT | FALSE_POSITIVE

    @Column(name = "reasons_json", length = 1000)
    private String reasonsJson;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}