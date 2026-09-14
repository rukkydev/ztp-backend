package com.ztp.threat;

import com.ztp.threat.dto.ThreatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ThreatService {

    private final ThreatRepository repository;
    private final com.ztp.risk.RiskEvaluationLogRepository riskLogRepository;

    public List<ThreatResponse> listAll() {
        return repository.findAllByOrderByCreatedAtDesc().stream().map(ThreatResponse::new).toList();
    }

    public ThreatResponse getById(Long id) {
        Threat threat = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Threat not found"));
        return new ThreatResponse(threat);
    }

    public ThreatResponse updateStatus(Long id, String status, String outcome) {
        Threat threat = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Threat not found"));
        threat.setStatus(status);
        if (outcome != null && !outcome.isBlank()) {
            threat.setOutcome(outcome);
            syncRiskEvaluationLabel(threat.getCorrelationId(), outcome);
        }
        if ("Mitigated".equals(status)) {
            threat.setMitigatedAt(LocalDateTime.now());
        }
        return new ThreatResponse(repository.save(threat));
    }

    public List<ThreatResponse> bulkMitigate(List<Long> ids, String outcome) {
        List<Threat> threats = repository.findAllById(ids);
        threats.forEach(t -> {
            t.setStatus("Mitigated");
            t.setMitigatedAt(LocalDateTime.now());
            if (outcome != null && !outcome.isBlank()) {
                t.setOutcome(outcome);
                syncRiskEvaluationLabel(t.getCorrelationId(), outcome);
            }
        });
        return repository.saveAll(threats).stream().map(ThreatResponse::new).toList();
    }

    private void syncRiskEvaluationLabel(String correlationId, String outcome) {
        if (correlationId == null || correlationId.isBlank()) return;
        riskLogRepository.findByCorrelationId(correlationId).ifPresent(log -> {
            String label = "FALSE_POSITIVE".equalsIgnoreCase(outcome) ? "legitimate" : "abuse";
            log.setLabel(label);
            riskLogRepository.save(log);
        });
    }
}