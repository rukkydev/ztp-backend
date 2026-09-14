package com.ztp.settings.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdateSettingsRequest {
    private General general;
    private Security security;

    @Getter
    @Setter
    public static class General {
        private String orgName;
        private String supportEmail;
    }

    @Getter
    @Setter
    public static class Security {
        private Integer sessionTimeout;
        private List<ToggleUpdate> toggles;
    }

    @Getter
    @Setter
    public static class ToggleUpdate {
        private String key;
        private boolean enabled;
    }
}