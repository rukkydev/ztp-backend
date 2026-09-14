package com.ztp.report;

import com.ztp.common.ApiResponse;
import com.ztp.report.dto.CreateReportRequest;
import com.ztp.report.dto.ReportResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping
    @PreAuthorize("hasAuthority('AUDIT_VIEW')")
    public ApiResponse<List<ReportResponse>> list() {
        return ApiResponse.success("Reports retrieved", reportService.listReports());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('AUDIT_VIEW')")
    public ApiResponse<ReportResponse> create(@Valid @RequestBody CreateReportRequest request) {
        String actor = SecurityContextHolder.getContext().getAuthentication().getName();
        return ApiResponse.success("Report generation started", reportService.createReport(request, actor));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('AUDIT_VIEW')")
    public ApiResponse<ReportResponse> get(@PathVariable Long id) {
        return ApiResponse.success("Report status retrieved", reportService.getReport(id));
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasAuthority('AUDIT_VIEW')")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        var report = reportService.getEntity(id);
        Resource file = reportService.loadFileForDownload(id);

        MediaType mediaType = "CSV".equals(report.getFormat()) ? MediaType.parseMediaType("text/csv") : MediaType.APPLICATION_PDF;
        String filename = report.getName().replace(" ", "_") + ("CSV".equals(report.getFormat()) ? ".csv" : ".pdf");

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(file);
    }
}