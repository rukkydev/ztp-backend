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

    public List<ThreatResponse> listAll() {
        return repository.findAllByOrderByCreatedAtDesc().stream().map(ThreatResponse::new).toList();
    }

    public ThreatResponse updateStatus(Long id, String status) {
        Threat threat = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Threat not found"));
        threat.setStatus(status);
        if ("Mitigated".equals(status)) {
            threat.setMitigatedAt(LocalDateTime.now());
        }
        return new ThreatResponse(repository.save(threat));
    }

    public List<ThreatResponse> bulkMitigate(List<Long> ids) {
        List<Threat> threats = repository.findAllById(ids);
        threats.forEach(t -> { t.setStatus("Mitigated"); t.setMitigatedAt(LocalDateTime.now()); });
        return repository.saveAll(threats).stream().map(ThreatResponse::new).toList();
    }
}