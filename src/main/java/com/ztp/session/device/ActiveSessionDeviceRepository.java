package com.ztp.session.device;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ActiveSessionDeviceRepository extends JpaRepository<ActiveSessionDevice, Long> {
    List<ActiveSessionDevice> findAllByUserIdAndDeviceId(Long userId, String deviceId);
}