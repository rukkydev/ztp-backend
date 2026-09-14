package com.ztp.report;

import com.ztp.audit.AuditLogRepository;
import com.ztp.threat.AlertRepository;
import com.ztp.threat.ThreatRepository;
import com.ztp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReportGenerator {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final ThreatRepository threatRepository;
    private final AlertRepository alertRepository;

    public void generate(String category, String format, Path outputPath) throws IOException {
        if ("CSV".equals(format)) {
            generateCsv(category, outputPath);
        } else {
            generatePdfSummary(category, outputPath);
        }
    }

    private void generateCsv(String category, Path outputPath) throws IOException {
        StringBuilder sb = new StringBuilder();

        switch (category) {
            case "USERS" -> {
                sb.append("id,username,email,role,enabled,accountLocked,createdAt\n");
                userRepository.findAll().forEach(u -> sb.append(String.join(",",
                        String.valueOf(u.getId()), u.getUsername(), u.getEmail(),
                        u.getRole().getName(), String.valueOf(u.isEnabled()),
                        String.valueOf(u.isAccountLocked()), String.valueOf(u.getCreatedAt())
                )).append("\n"));
            }
            case "AUDIT_LOGS" -> {
                sb.append("id,eventType,actorUsername,description,ipAddress,createdAt\n");
                auditLogRepository.findAllByOrderByCreatedAtDesc().forEach(l -> sb.append(String.join(",",
                        String.valueOf(l.getId()), l.getEventType(),
                        safe(l.getActorUsername()), safe(l.getDescription()),
                        safe(l.getIpAddress()), String.valueOf(l.getCreatedAt())
                )).append("\n"));
            }
            case "THREATS" -> {
                sb.append("id,title,severity,status,username,createdAt\n");
                threatRepository.findAllByOrderByCreatedAtDesc().forEach(t -> sb.append(String.join(",",
                        String.valueOf(t.getId()), safe(t.getTitle()), t.getSeverity(),
                        t.getStatus(), safe(t.getUsername()), String.valueOf(t.getCreatedAt())
                )).append("\n"));
            }
            case "ALERTS" -> {
                sb.append("id,title,severity,status,username,createdAt\n");
                alertRepository.findAllByOrderByCreatedAtDesc().forEach(a -> sb.append(String.join(",",
                        String.valueOf(a.getId()), safe(a.getTitle()), a.getSeverity(),
                        a.getStatus(), safe(a.getUsername()), String.valueOf(a.getCreatedAt())
                )).append("\n"));
            }
            default -> throw new IllegalArgumentException("CSV export not supported for category: " + category);
        }

        java.nio.file.Files.writeString(outputPath, sb.toString());
    }

    private void generatePdfSummary(String category, Path outputPath) throws IOException {
        List<String> lines = buildSummaryLines(category);

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(doc, page)) {
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
                content.beginText();
                content.newLineAtOffset(50, 750);
                content.showText("ZTP Security Report: " + category.replace("_", " "));
                content.endText();

                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
                float y = 710;
                for (String line : lines) {
                    content.beginText();
                    content.newLineAtOffset(50, y);
                    content.showText(line);
                    content.endText();
                    y -= 20;
                }
            }

            doc.save(outputPath.toFile());
        }
    }

    private List<String> buildSummaryLines(String category) {
        return switch (category) {
            case "USERS" -> {
                var users = userRepository.findAll();
                yield List.of(
                        "Total users: " + users.size(),
                        "Locked accounts: " + users.stream().filter(u -> u.isAccountLocked()).count(),
                        "Suspended accounts: " + users.stream().filter(u -> !u.isEnabled()).count()
                );
            }
            case "AUDIT_LOGS" -> {
                var logs = auditLogRepository.findAllByOrderByCreatedAtDesc();
                yield List.of(
                        "Total events recorded: " + logs.size(),
                        "Login successes: " + logs.stream().filter(l -> "LOGIN_SUCCESS".equals(l.getEventType())).count(),
                        "Login failures: " + logs.stream().filter(l -> "LOGIN_FAILED".equals(l.getEventType())).count()
                );
            }
            case "THREATS" -> {
                var threats = threatRepository.findAllByOrderByCreatedAtDesc();
                yield List.of(
                        "Total threats detected: " + threats.size(),
                        "Active: " + threats.stream().filter(t -> "Active".equals(t.getStatus())).count(),
                        "Mitigated: " + threats.stream().filter(t -> "Mitigated".equals(t.getStatus())).count()
                );
            }
            case "ALERTS" -> {
                var alerts = alertRepository.findAllByOrderByCreatedAtDesc();
                yield List.of(
                        "Total alerts raised: " + alerts.size(),
                        "Open: " + alerts.stream().filter(a -> "Open".equals(a.getStatus())).count(),
                        "Resolved: " + alerts.stream().filter(a -> "Resolved".equals(a.getStatus())).count()
                );
            }
            case "EXECUTIVE_SUMMARY" -> List.of(
                    "Total users: " + userRepository.count(),
                    "Total security events: " + auditLogRepository.count(),
                    "Active threats: " + threatRepository.findAllByOrderByCreatedAtDesc().stream()
                            .filter(t -> "Active".equals(t.getStatus())).count(),
                    "Open alerts: " + alertRepository.findAllByOrderByCreatedAtDesc().stream()
                            .filter(a -> "Open".equals(a.getStatus())).count(),
                    "Generated: " + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            );
            default -> List.of("No summary available for this category.");
        };
    }

    private String safe(String value) {
        return value == null ? "" : value.replace(",", ";"); // avoid breaking CSV columns
    }
}