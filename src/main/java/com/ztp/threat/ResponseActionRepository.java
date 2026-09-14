package com.ztp.threat;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ResponseActionRepository extends JpaRepository<ResponseAction, Long> {
    List<ResponseAction> findAllByCorrelationId(String correlationId);
    List<ResponseAction> findAllByUserIdOrderByExecutedAtDesc(Long userId);
}
