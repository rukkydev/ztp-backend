package com.ztp.settings;

import com.ztp.settings.dto.SettingsResponse;
import com.ztp.settings.dto.ToggleResponse;
import com.ztp.settings.dto.UpdateSettingsRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final PlatformSettingsRepository settingsRepository;
    private final SecurityToggleRepository toggleRepository;

    public SettingsResponse getSettings() {
        PlatformSettings settings = getOrCreateSettings();
        ensureTogglesSeeded();

        List<ToggleResponse> toggles = toggleRepository.findAll().stream()
                .map(t -> new ToggleResponse(t.getToggleKey(), t.getLabel(), t.isEnabled()))
                .toList();

        return new SettingsResponse(
                new SettingsResponse.General(settings.getOrgName(), settings.getSupportEmail()),
                new SettingsResponse.Security(settings.getSessionTimeoutMinutes(), toggles)
        );
    }

    public SettingsResponse updateSettings(UpdateSettingsRequest request) {
        PlatformSettings settings = getOrCreateSettings();

        if (request.getGeneral() != null) {
            if (request.getGeneral().getOrgName() != null) {
                settings.setOrgName(request.getGeneral().getOrgName());
            }
            if (request.getGeneral().getSupportEmail() != null) {
                settings.setSupportEmail(request.getGeneral().getSupportEmail());
            }
        }

        if (request.getSecurity() != null) {
            if (request.getSecurity().getSessionTimeout() != null) {
                settings.setSessionTimeoutMinutes(request.getSecurity().getSessionTimeout());
            }
            if (request.getSecurity().getToggles() != null) {
                for (var toggleUpdate : request.getSecurity().getToggles()) {
                    toggleRepository.findByToggleKey(toggleUpdate.getKey()).ifPresent(t -> {
                        t.setEnabled(toggleUpdate.isEnabled());
                        toggleRepository.save(t);
                    });
                }
            }
        }

        settingsRepository.save(settings);
        return getSettings();
    }

    private PlatformSettings getOrCreateSettings() {
        return settingsRepository.findById(1L).orElseGet(() -> {
            PlatformSettings settings = new PlatformSettings();
            settings.setId(1L);
            return settingsRepository.save(settings);
        });
    }

    private void ensureTogglesSeeded() {
        if (toggleRepository.count() > 0) return;

        for (var def : SettingsDefaults.DEFAULT_TOGGLES) {
            SecurityToggle toggle = new SecurityToggle();
            toggle.setToggleKey(def.key());
            toggle.setLabel(def.label());
            toggle.setEnabled(def.defaultEnabled());
            toggleRepository.save(toggle);
        }
    }
}