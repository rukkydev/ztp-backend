package com.ztp.report;

import com.ztp.report.dto.CreateReportRequest;
import com.ztp.report.dto.ReportResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final ReportGenerator generator;

    @Value("${app.reports-dir}")
    private String reportsDir;

    public List<ReportResponse> listReports() {
        return reportRepository.findAllByOrderByGeneratedAtDesc().stream().map(ReportResponse::new).toList();
    }

    public ReportResponse createReport(CreateReportRequest request, String actorName) {
        Report report = new Report();
        report.setCategory(request.getCategory());
        report.setType(request.getType());
        report.setFormat(request.getFormat());
        report.setGeneratedBy(actorName);
        report.setName(buildReportName(request.getCategory(), request.getFormat()));

        Report saved = reportRepository.save(report);

        generateAsync(saved.getId(), request.getCategory(), request.getFormat());

        return new ReportResponse(saved);
    }

    public ReportResponse getReport(Long id) {
        return reportRepository.findById(id)
                .map(ReportResponse::new)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));
    }

    public Resource loadFileForDownload(Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));

        if (!"Ready".equals(report.getStatus())) {
            throw new IllegalStateException("Report is not ready yet");
        }

        try {
            return new UrlResource(Path.of(report.getFilePath()).toUri());
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Report file could not be located", e);
        }
    }

    public Report getEntity(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));
    }

    @Async
    public void generateAsync(Long reportId, String category, String format) {
        Report report = reportRepository.findById(reportId).orElse(null);
        if (report == null) return;

        try {
            Files.createDirectories(Path.of(reportsDir));

            String extension = "CSV".equals(format) ? ".csv" : ".pdf";
            String filename = "report-" + reportId + "-" + UUID.randomUUID() + extension;
            Path outputPath = Path.of(reportsDir, filename);

            generator.generate(category, format, outputPath);

            report.setFilePath(outputPath.toString());
            report.setFileSizeBytes(Files.size(outputPath));
            report.setStatus("Ready");
        } catch (IOException | IllegalArgumentException e) {
            report.setStatus("Failed");
        }

        reportRepository.save(report);
    }

    private String buildReportName(String category, String format) {
        String label = category.replace("_", " ");
        String date = java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
        return label + " Export — " + date;
    }
}