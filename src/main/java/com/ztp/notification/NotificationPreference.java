package com.ztp.notification;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notification_preferences", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "channel", "event_key"}))
@Getter
@Setter
@NoArgsConstructor
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 10)
    private String channel; // "email" | "push"

    @Column(name = "event_key", nullable = false, length = 100)
    private String eventKey; // e.g. "login_alerts", "security_updates"

    @Column(nullable = false)
    private boolean checked = false;
}