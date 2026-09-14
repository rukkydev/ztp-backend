package com.ztp.threat;

import com.ztp.threat.dto.AlertResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository repository;
    private final com.ztp.risk.RiskEvaluationLogRepository riskLogRepository;

    public List<AlertResponse> listAll() {
        return repository.findAllByOrderByCreatedAtDesc().stream().map(AlertResponse::new).toList();
    }

    public AlertResponse getById(Long id) {
        Alert alert = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found"));
        return new AlertResponse(alert);
    }

    public AlertResponse updateStatus(Long id, String status, String outcome) {
        Alert alert = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found"));
        alert.setStatus(status);
        if (outcome != null && !outcome.isBlank()) {
            alert.setOutcome(outcome);
            syncRiskEvaluationLabel(alert.getCorrelationId(), outcome);
        }
        if ("Resolved".equals(status)) {
            alert.setResolvedAt(LocalDateTime.now());
        }
        return new AlertResponse(repository.save(alert));
    }

    public List<AlertResponse> bulkResolve(List<Long> ids, String outcome) {
        List<Alert> alerts = repository.findAllById(ids);
        alerts.forEach(a -> {
            a.setStatus("Resolved");
            a.setResolvedAt(LocalDateTime.now());
            if (outcome != null && !outcome.isBlank()) {
                a.setOutcome(outcome);
                syncRiskEvaluationLabel(a.getCorrelationId(), outcome);
            }
        });
        return repository.saveAll(alerts).stream().map(AlertResponse::new).toList();
    }

    private void syncRiskEvaluationLabel(String correlationId, String outcome) {
        if (correlationId == null || correlationId.isBlank()) return;
        riskLogRepository.findByCorrelationId(correlationId).ifPresent(log -> {
            // Label maps to legitimate (if false positive) or abuse (if confirmed threat)
            String label = "FALSE_POSITIVE".equalsIgnoreCase(outcome) ? "legitimate" : "abuse";
            log.setLabel(label);
            riskLogRepository.save(log);
        });
    }
}