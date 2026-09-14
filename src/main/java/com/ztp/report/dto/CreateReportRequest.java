package com.ztp.report.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateReportRequest {
    @NotBlank
    private String category; // USERS | AUDIT_LOGS | THREATS | ALERTS | EXECUTIVE_SUMMARY

    @NotBlank
    private String type; // Security | Compliance | Usage

    @NotBlank
    private String format; // CSV | PDF
}