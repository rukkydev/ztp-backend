package com.ztp.settings;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SecurityToggleRepository extends JpaRepository<SecurityToggle, Long> {
    Optional<SecurityToggle> findByToggleKey(String key);
}