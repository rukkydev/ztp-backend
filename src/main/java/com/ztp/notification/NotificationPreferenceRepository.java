package com.ztp.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {
    List<NotificationPreference> findAllByUserId(Long userId);
    Optional<NotificationPreference> findByUserIdAndChannelAndEventKey(Long userId, String channel, String eventKey);
}