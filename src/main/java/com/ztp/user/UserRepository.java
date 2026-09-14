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
}