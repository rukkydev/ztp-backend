package com.ztp.report;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 30)
    private String category; // USERS | AUDIT_LOGS | THREATS | ALERTS | EXECUTIVE_SUMMARY

    @Column(nullable = false, length = 30)
    private String type; // Security | Compliance | Usage

    @Column(nullable = false, length = 10)
    private String format; // CSV | PDF

    @Column(nullable = false, length = 20)
    private String status = "Processing"; // Processing | Ready | Failed

    @Column(name = "generated_at", nullable = false, updatable = false)
    private LocalDateTime generatedAt;

    @Column(name = "generated_by", length = 150)
    private String generatedBy;

    @Column(name = "file_path", length = 300)
    private String filePath;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @PrePersist
    protected void onCreate() {
        this.generatedAt = LocalDateTime.now();
    }
}