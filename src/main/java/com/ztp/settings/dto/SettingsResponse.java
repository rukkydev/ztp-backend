package com.ztp.settings.dto;

import lombok.Getter;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
public class SettingsResponse {

    @Getter
    @AllArgsConstructor
    public static class General {
        private final String orgName;
        private final String supportEmail;
    }

    @Getter
    @AllArgsConstructor
    public static class Security {
        private final int sessionTimeout;
        private final List<ToggleResponse> toggles;
    }

    private final General general;
    private final Security security;
}