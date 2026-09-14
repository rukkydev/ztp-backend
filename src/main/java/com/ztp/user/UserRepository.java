package com.ztp.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Data access layer for User.
 * We NEVER write raw SQL here unless it's something Spring Data JPA
 * genuinely can't express (rare). Method names below are turned into
 * SQL automatically by Spring Data at startup.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @org.springframework.data.jpa.repository.Query("""
        SELECT u FROM User u
        WHERE (:query IS NULL OR :query = '' OR LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')))
          AND (:enabled IS NULL OR u.enabled = :enabled)
        """)
    org.springframework.data.domain.Page<User> searchUsers(
            @org.springframework.data.repository.query.Param("query") String query,
            @org.springframework.data.repository.query.Param("enabled") Boolean enabled,
            org.springframework.data.domain.Pageable pageable);
}