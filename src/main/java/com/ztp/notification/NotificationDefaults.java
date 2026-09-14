package com.ztp.notification;

import java.util.List;

/**
 * The default set of toggles every user starts with. Kept as a constant
 * list rather than hardcoded per-service, so it's one place to add a
 * new notification type later.
 */
public class NotificationDefaults {

    public record Entry(String channel, String eventKey, String label, boolean defaultChecked) {}

    public static final List<Entry> DEFAULTS = List.of(
            new Entry("email", "login_alerts", "New login detected", true),
            new Entry("email", "security_updates", "Security policy updates", true),
            new Entry("email", "account_changes", "Changes to your account", true),
            new Entry("push", "login_alerts", "New login detected", false),
            new Entry("push", "critical_alerts", "Critical security alerts", true)
    );
}