package com.ztp.otp;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OneTimeCodeRepository extends JpaRepository<OneTimeCode, Long> {
    List<OneTimeCode> findAllByUserIdAndPurposeAndConsumedFalse(Long userId, String purpose);
}