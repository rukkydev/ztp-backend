package com.ztp.notification.dto;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class NotificationItemResponse {
    private final String eventKey;
    private final String label;
    private final boolean checked;
}