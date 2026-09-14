package com.ztp.notification.inapp.dto;

import com.ztp.notification.inapp.Notification;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class NotificationResponse {
    private final Long id;
    private final String eventKey;
    private final String title;
    private final String message;
    private final boolean read;
    private final LocalDateTime createdAt;

    public NotificationResponse(Notification n) {
        this.id = n.getId();
        this.eventKey = n.getEventKey();
        this.title = n.getTitle();
        this.message = n.getMessage();
        this.read = n.isRead();
        this.createdAt = n.getCreatedAt();
    }
}