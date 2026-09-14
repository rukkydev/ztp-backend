package com.ztp.notification.dto;

import lombok.Getter;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
public class NotificationPreferencesResponse {
    private final List<NotificationItemResponse> email;
    private final List<NotificationItemResponse> push;
}