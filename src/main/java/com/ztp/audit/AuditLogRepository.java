package com.ztp.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findAllByOrderByCreatedAtDesc();
    List<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("""
        SELECT a FROM AuditLog a
        WHERE (:eventType IS NULL OR a.eventType = :eventType)
          AND (:from IS NULL OR a.createdAt >= :from)
          AND (:to IS NULL OR a.createdAt <= :to)
        ORDER BY a.createdAt DESC
        """)
    Page<AuditLog> search(@Param("eventType") String eventType,
                           @Param("from") LocalDateTime from,
                           @Param("to") LocalDateTime to,
                           Pageable pageable);
						   
	boolean existsByActorUserIdAndIpAddress(Long actorUserId, String ipAddress);

	long countByActorUserIdAndEventTypeAndCreatedAtAfter(Long actorUserId, String eventType, LocalDateTime after);

	@Query("SELECT a.createdAt FROM AuditLog a " +
		   "WHERE a.actorUserId = :userId AND a.eventType IN ('LOGIN_SUCCESS','LOGIN_FAILED') " +
		   "ORDER BY a.createdAt DESC")
	List<LocalDateTime> findRecentLoginTimestamps(@Param("userId") Long userId, Pageable pageable);
}

