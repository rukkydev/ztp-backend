package com.ztp.settings.dto;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class ToggleResponse {
    private final String key;
    private final String label;
    private final boolean enabled;
}