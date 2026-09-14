package com.ztp.threat;

import com.ztp.audit.AuditLog;
import com.ztp.audit.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ThreatDetectionService {

    private final AlertRepository alertRepository;
    private final ThreatRepository threatRepository;
    private final AuditLogRepository auditLogRepository;

    public void evaluate(AuditLog log) {
        switch (log.getEventType()) {
            case "ACCOUNT_LOCKED" -> raiseAlert(log,
                    "Account locked",
                    "Account locked after repeated failed login attempts",
                    "Medium");

            case "DEVICE_VERIFICATION_CODE_SENT" -> raiseAlert(log,
                    "Unrecognized device login attempt",
                    "Login attempted from a device not previously seen for this account",
                    "Low");

            case "LOGIN_BLOCKED_DEVICE" -> raiseThreat(log,
                    "Login attempt from blocked device",
                    "A blocked device attempted to authenticate with valid credentials",
                    "High");

            case "TWO_FACTOR_FAILED" -> checkRepeatedTwoFactorFailures(log);

            default -> { }
        }
    }

	private void checkRepeatedTwoFactorFailures(AuditLog log) {
    if (log.getActorUserId() == null) return;

    LocalDateTime windowStart = LocalDateTime.now().minusMinutes(10);

    boolean alreadyRaised = threatRepository.existsByUserIdAndTitleAndStatusAndCreatedAtAfter(
            log.getActorUserId(), "Possible 2FA brute-force attempt", "Active", windowStart);
    if (alreadyRaised) return;

    long recentFailures = auditLogRepository.countByActorUserIdAndEventTypeAndCreatedAtAfter(
            log.getActorUserId(), "TWO_FACTOR_FAILED", windowStart);

    if (recentFailures >= 3) {
        raiseThreat(log, "Possible 2FA brute-force attempt",
                "3 or more failed 2FA attempts within 10 minutes", "High");
    }
}

    private void raiseAlert(AuditLog log, String title, String description, String severity) {
        Alert alert = new Alert();
        alert.setTitle(title);
        alert.setDescription(description);
        alert.setSeverity(severity);
        alert.setUserId(log.getActorUserId());
        alert.setUsername(log.getActorUsername());
        alertRepository.save(alert);
    }

    private void raiseThreat(AuditLog log, String title, String description, String severity) {
        Threat threat = new Threat();
        threat.setTitle(title);
        threat.setDescription(description);
        threat.setSeverity(severity);
        threat.setUserId(log.getActorUserId());
        threat.setUsername(log.getActorUsername());
        threatRepository.save(threat);
    }
}