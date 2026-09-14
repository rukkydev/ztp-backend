package com.ztp.session.dto;

import lombok.Getter;
import lombok.AllArgsConstructor;

import java.util.Date;

@Getter
@AllArgsConstructor
public class SessionResponse {
    private final String sessionId;      // full id, needed for revoke calls
    private final String displaySessionId; // masked, safe to show in UI
    private final String username;
    private final Date lastRequest;

    public SessionResponse(String sessionId, String username, Date lastRequest) {
        this.sessionId = sessionId;
        this.displaySessionId = maskSessionId(sessionId);
        this.username = username;
        this.lastRequest = lastRequest;
    }

    private static String maskSessionId(String id) {
        if (id == null || id.length() < 8) return "••••••••";
        return id.substring(0, 4) + "••••" + id.substring(id.length() - 4);
    }
}