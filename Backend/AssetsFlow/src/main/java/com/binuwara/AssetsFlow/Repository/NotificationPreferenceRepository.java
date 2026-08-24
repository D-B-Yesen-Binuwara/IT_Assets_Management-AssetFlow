package com.binuwara.AssetsFlow.Repository;

import com.binuwara.AssetsFlow.Entity.NotificationPreference;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, UUID> {
    @EntityGraph(attributePaths = {"user"})
    List<NotificationPreference> findAllByUser_IdOrderByNotificationTypeAsc(UUID userId);
    Optional<NotificationPreference> findByUser_IdAndNotificationTypeIgnoreCase(UUID userId, String notificationType);
}
