package com.ztp.settings;

import com.ztp.common.ApiResponse;
import com.ztp.settings.dto.SettingsResponse;
import com.ztp.settings.dto.UpdateSettingsRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping
    @PreAuthorize("hasAuthority('SETTINGS_MANAGE')")
    public ApiResponse<SettingsResponse> get() {
        return ApiResponse.success("Settings retrieved", settingsService.getSettings());
    }

    @PatchMapping
    @PreAuthorize("hasAuthority('SETTINGS_MANAGE')")
    public ApiResponse<SettingsResponse> update(@Valid @RequestBody UpdateSettingsRequest request) {
        return ApiResponse.success("Settings updated", settingsService.updateSettings(request));
    }
}