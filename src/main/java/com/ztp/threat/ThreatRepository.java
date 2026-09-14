package com.ztp.threat;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.time.LocalDateTime;

public interface ThreatRepository extends JpaRepository<Threat, Long> {
    List<Threat> findAllByOrderByCreatedAtDesc();
	boolean existsByUserIdAndTitleAndStatusAndCreatedAtAfter(Long userId, String title, String status, LocalDateTime after);
}