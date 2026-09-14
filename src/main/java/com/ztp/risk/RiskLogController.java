package com.ztp.risk;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
public class RiskLogController {

    private final RiskLogService riskLogService;

    @GetMapping("/api/admin/risk-logs/export")
    @PreAuthorize("hasAuthority('SETTINGS_MANAGE')")
    public ResponseEntity<String> export(@RequestParam String from, @RequestParam String to) {
        String jsonl = riskLogService.exportAsJsonLines(
                LocalDate.parse(from).atStartOfDay(),
                LocalDate.parse(to).atTime(23, 59, 59));

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"risk-logs-" + from + "-to-" + to + ".jsonl\"")
                .body(jsonl);
    }
}