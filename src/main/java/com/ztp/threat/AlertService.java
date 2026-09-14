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

    public List<AlertResponse> listAll() {
        return repository.findAllByOrderByCreatedAtDesc().stream().map(AlertResponse::new).toList();
    }

    public AlertResponse updateStatus(Long id, String status) {
        Alert alert = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found"));
        alert.setStatus(status);
        if ("Resolved".equals(status)) {
            alert.setResolvedAt(LocalDateTime.now());
        }
        return new AlertResponse(repository.save(alert));
    }

    public List<AlertResponse> bulkResolve(List<Long> ids) {
        List<Alert> alerts = repository.findAllById(ids);
        alerts.forEach(a -> { a.setStatus("Resolved"); a.setResolvedAt(LocalDateTime.now()); });
        return repository.saveAll(alerts).stream().map(AlertResponse::new).toList();
    }
}