package com.ztp.risk;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface RiskEvaluationLogRepository extends JpaRepository<RiskEvaluationLog, Long> {
    List<RiskEvaluationLog> findAllByEvaluatedAtBetweenOrderByEvaluatedAtAsc(LocalDateTime from, LocalDateTime to);
    java.util.Optional<RiskEvaluationLog> findByCorrelationId(String correlationId);
}