package com.ztp.report.dto;

import com.ztp.report.Report;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReportResponse {
    private final Long id;
    private final String name;
    private final String type;
    private final String format;
    private final String status;
    private final LocalDateTime generatedAt;
    private final String generatedBy;
    private final String fileSize;

    public ReportResponse(Report r) {
        this.id = r.getId();
        this.name = r.getName();
        this.type = r.getType();
        this.format = r.getFormat();
        this.status = r.getStatus();
        this.generatedAt = r.getGeneratedAt();
        this.generatedBy = r.getGeneratedBy();
        this.fileSize = formatSize(r.getFileSizeBytes());
    }

    private String formatSize(Long bytes) {
        if (bytes == null) return "—";
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024));
    }
}