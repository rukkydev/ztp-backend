package com.ztp.notification.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdateNotificationPreferencesRequest {
    private List<ItemUpdate> email;
    private List<ItemUpdate> push;

    @Getter
    @Setter
    public static class ItemUpdate {
        private String eventKey;
        private boolean checked;
    }
}