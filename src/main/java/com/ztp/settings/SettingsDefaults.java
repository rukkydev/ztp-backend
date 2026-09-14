package com.ztp.settings;

import java.util.List;

public class SettingsDefaults {

    public record Entry(String key, String label, boolean defaultEnabled) {}

    public static final List<Entry> DEFAULT_TOGGLES = List.of(
            new Entry("require_two_factor", "Require 2FA for all users", true),
            new Entry("require_device_verification", "Require device verification", true),
            new Entry("allow_self_registration", "Allow public self-registration", true),
            new Entry("allow_password_reset", "Allow self-service password reset", true)
    );
}