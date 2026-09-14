package com.ztp.audit;

import com.ztp.audit.dto.AuditLogResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import com.ztp.threat.ThreatDetectionService;
import com.ztp.notification.inapp.NotificationService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ThreatDetectionService threatDetectionService;
    private final NotificationService notificationService;
	
	private static final int LOGIN_HISTORY_SAMPLE_SIZE = 200;

    public void record(String eventType, Long actorUserId, String actorUsername,
                        String description, HttpServletRequest request) {
        record(eventType, actorUserId, actorUsername, description, request, null);
    }

    public void record(String eventType, Long actorUserId, String actorUsername,
                        String description, HttpServletRequest request, Long targetUserId) {
        String ip = resolveIp(request);

        AuditLog log = AuditLog.builder()
                .eventType(eventType)
                .actorUserId(actorUserId)
                .actorUsername(actorUsername)
                .description(description)
                .ipAddress(ip)
                .targetUserId(targetUserId)
                .deviceId(request != null ? request.getHeader("X-Device-Id") : null)
                .browser(request != null ? UserAgentParser.parseBrowser(request.getHeader("User-Agent")) : null)
                .operatingSystem(request != null ? UserAgentParser.parseOperatingSystem(request.getHeader("User-Agent")) : null)
                .isNewIp(actorUserId != null ? computeIsNewIp(actorUserId, ip) : null)
                .loginHourUnusual(actorUserId != null && isLoginEvent(eventType) ? computeLoginHourUnusual(actorUserId) : null)
                .build();

        auditLogRepository.save(log);
        threatDetectionService.evaluate(log);
        notificationService.evaluate(log);
    }

    private boolean isLoginEvent(String eventType) {
        return "LOGIN_SUCCESS".equals(eventType) || "LOGIN_FAILED".equals(eventType);
    }
	

public boolean computeIsNewIp(Long userId, String ip) {
    if (ip == null) return false;
    return !auditLogRepository.existsByActorUserIdAndIpAddress(userId, ip);
}

public boolean computeLoginHourUnusual(Long userId) {
    List<LocalDateTime> recentLogins = auditLogRepository.findRecentLoginTimestamps(
            userId, org.springframework.data.domain.PageRequest.of(0, LOGIN_HISTORY_SAMPLE_SIZE));

    if (recentLogins.size() < 5) return false;

    int currentHour = LocalDateTime.now().getHour();
    long withinTypicalWindow = recentLogins.stream()
            .filter(t -> Math.abs(t.getHour() - currentHour) <= 2)
            .count();

    return (double) withinTypicalWindow / recentLogins.size() < 0.10;
}

public int countRecentFailedLogins(Long userId, int minutes) {
    LocalDateTime windowStart = LocalDateTime.now().minusMinutes(minutes);
    return (int) auditLogRepository.countByActorUserIdAndEventTypeAndCreatedAtAfter(userId, "LOGIN_FAILED", windowStart);
}

public int countRecentTwoFactorFailures(Long userId, int minutes) {
    LocalDateTime windowStart = LocalDateTime.now().minusMinutes(minutes);
    return (int) auditLogRepository.countByActorUserIdAndEventTypeAndCreatedAtAfter(userId, "TWO_FACTOR_FAILED", windowStart);
}
	
    public com.ztp.common.PagedResponse<AuditLogResponse> search(String eventType, String from, String to,
                                                                   int page, int pageSize) {
        LocalDateTime fromDate = (from != null && !from.isBlank())
                ? java.time.LocalDate.parse(from).atStartOfDay() : null;
        LocalDateTime toDate = (to != null && !to.isBlank())
                ? java.time.LocalDate.parse(to).atTime(23, 59, 59) : null;

        var result = auditLogRepository.search(
                eventType, fromDate, toDate,
                org.springframework.data.domain.PageRequest.of(page, pageSize));

        var responses = result.getContent().stream().map(AuditLogResponse::new).toList();
        return new com.ztp.common.PagedResponse<>(responses, result.getTotalElements(), page, pageSize);
    }

    public AuditLogResponse getById(Long id) {
        return auditLogRepository.findById(id)
                .map(AuditLogResponse::new)
                .orElseThrow(() -> new IllegalArgumentException("Activity log entry not found"));
    }

    public String currentActorEmail() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? auth.getName() : "unknown";
    }

    private String resolveIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}