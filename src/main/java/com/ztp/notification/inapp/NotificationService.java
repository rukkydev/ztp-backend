package com.ztp.notification.inapp;

import com.ztp.audit.AuditLog;
import com.ztp.notification.NotificationPreferenceRepository;
import com.ztp.notification.inapp.dto.NotificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
	
	public void evaluate(AuditLog log) {
    Long recipientId = log.getTargetUserId() != null ? log.getTargetUserId() : log.getActorUserId();
    if (recipientId == null) return;

    switch (log.getEventType()) {
        case "LOGIN_SUCCESS", "TWO_FACTOR_VERIFIED" -> notifyIfEnabled(recipientId, "login_alerts",
                "New login detected", "A new login was recorded on your account.");
        case "LOGIN_BLOCKED_DEVICE" -> notifyIfEnabled(recipientId, "critical_alerts",
                "Blocked device attempted login", "A device you previously blocked attempted to sign in.");
        case "ACCOUNT_LOCKED" -> notifyIfEnabled(recipientId, "account_changes",
                "Your account was locked", "Your account was locked after repeated failed login attempts.");
        case "DEVICE_VERIFIED" -> notifyIfEnabled(recipientId, "account_changes",
                "New device verified", "A new device was verified on your account.");
        case "PASSWORD_RESET_COMPLETED" -> notifyIfEnabled(recipientId, "security_updates",
                "Password reset", "Your password was successfully reset.");
        case "ACCOUNT_UNLOCKED" -> notifyIfEnabled(recipientId, "account_changes",
                "Account unlocked", "An administrator unlocked your account.");
        case "USER_STATUS_CHANGED" -> notifyIfEnabled(recipientId, "account_changes",
                "Account status changed", "An administrator changed your account's status.");
        default -> { }
    }
}
	

    public List<NotificationResponse> listMyNotifications(Long userId) {
        return notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(NotificationResponse::new)
                .toList();
    }

    public long unreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    public void markRead(Long userId, Long notificationId) {
        Notification n = notificationRepository.findById(notificationId)
                .filter(existing -> existing.getUserId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        n.setRead(true);
        notificationRepository.save(n);
    }

    public void markAllRead(Long userId) {
        notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                .forEach(n -> { n.setRead(true); notificationRepository.save(n); });
    }

    private void notifyIfEnabled(Long userId, String eventKey, String title, String message) {
    if (!isPushEnabled(userId, eventKey)) return;

    Notification n = new Notification();
    n.setUserId(userId);
    n.setEventKey(eventKey);
    n.setTitle(title);
    n.setMessage(message);
    notificationRepository.save(n);
}

    private boolean isPushEnabled(Long userId, String eventKey) {
    var pushPref = preferenceRepository.findByUserIdAndChannelAndEventKey(userId, "push", eventKey);

    // If push preferences haven't been seeded yet for this user, default to
    // notifying -- safer than silently going dark before they've ever
    // visited Settings.
		return pushPref.map(p -> p.isChecked()).orElse(true);
	}
}