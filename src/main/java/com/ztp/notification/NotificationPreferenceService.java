package com.ztp.notification;

import com.ztp.notification.dto.NotificationItemResponse;
import com.ztp.notification.dto.NotificationPreferencesResponse;
import com.ztp.notification.dto.UpdateNotificationPreferencesRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationPreferenceService {

    private final NotificationPreferenceRepository repository;

    public NotificationPreferencesResponse getMyPreferences(Long userId) {
        ensureSeeded(userId);

        List<NotificationPreference> rows = repository.findAllByUserId(userId);
        Map<String, String> labels = labelLookup();

        List<NotificationItemResponse> email = rows.stream()
                .filter(r -> r.getChannel().equals("email"))
                .map(r -> toItem(r, labels))
                .toList();

        List<NotificationItemResponse> push = rows.stream()
                .filter(r -> r.getChannel().equals("push"))
                .map(r -> toItem(r, labels))
                .toList();

        return new NotificationPreferencesResponse(email, push);
    }

    public NotificationPreferencesResponse updateMyPreferences(Long userId, UpdateNotificationPreferencesRequest request) {
        applyUpdates(userId, "email", request.getEmail());
        applyUpdates(userId, "push", request.getPush());
        return getMyPreferences(userId);
    }

    private void applyUpdates(Long userId, String channel, List<UpdateNotificationPreferencesRequest.ItemUpdate> updates) {
        if (updates == null) return;
        for (var update : updates) {
            repository.findByUserIdAndChannelAndEventKey(userId, channel, update.getEventKey())
                    .ifPresent(pref -> {
                        pref.setChecked(update.isChecked());
                        repository.save(pref);
                    });
        }
    }

    private void ensureSeeded(Long userId) {
        if (!repository.findAllByUserId(userId).isEmpty()) return;

        for (var def : NotificationDefaults.DEFAULTS) {
            NotificationPreference pref = new NotificationPreference();
            pref.setUserId(userId);
            pref.setChannel(def.channel());
            pref.setEventKey(def.eventKey());
            pref.setChecked(def.defaultChecked());
            repository.save(pref);
        }
    }

    private NotificationItemResponse toItem(NotificationPreference row, Map<String, String> labels) {
        return new NotificationItemResponse(row.getEventKey(), labels.get(row.getEventKey()), row.isChecked());
    }

    private Map<String, String> labelLookup() {
        Map<String, String> map = new java.util.HashMap<>();
        NotificationDefaults.DEFAULTS.forEach(d -> map.put(d.eventKey(), d.label()));
        return map;
    }
}