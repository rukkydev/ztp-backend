package com.ztp.risk;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "risk_evaluation_logs")
@Getter
@Setter
@NoArgsConstructor
public class RiskEvaluationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "correlation_id", nullable = false, length = 64)
    private String correlationId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "event_type", length = 50)
    private String eventType;

    private boolean isNewDevice;
    private boolean isNewIp;
    private boolean loginHourUnusual;

    @Column(name = "failed_logins_last_10min")
    private int failedLoginsLast10Min;

    @Column(name = "two_factor_failures_last_10min")
    private int twoFactorFailuresLast10Min;

    @Column(name = "device_id", length = 100)
    private String deviceId;

    @Column(length = 100)
    private String browser;

    @Column(name = "operating_system", length = 100)
    private String operatingSystem;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "risk_score")
    private int riskScore;

    @Column(name = "risk_level", length = 20)
    private String riskLevel;

    @Column(name = "recommended_action", length = 20)
    private String recommendedAction;

    @Column(name = "enforced_action", length = 20)
    private String enforcedAction;

    @Column(name = "evaluate_reached", nullable = false)
    private boolean evaluateReached;
	
	@Column(name = "reasons_json", length = 1000)
	private String reasonsJson;

    private Boolean loginSucceeded;
    private Boolean challengeCompleted;
    private Boolean challengeFailed;
    private Boolean accountLocked;

    @Column(length = 20)
    private String label; // legitimate | abuse | unknown -- filled in later, never invented

    @Column(name = "evaluated_at", nullable = false, updatable = false)
    private LocalDateTime evaluatedAt;

    @PrePersist
    protected void onCreate() {
        this.evaluatedAt = LocalDateTime.now();
    }
}