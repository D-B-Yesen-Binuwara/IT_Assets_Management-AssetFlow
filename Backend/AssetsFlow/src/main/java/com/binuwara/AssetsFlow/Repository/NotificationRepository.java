package com.binuwara.AssetsFlow.Repository;

import com.binuwara.AssetsFlow.Entity.Notification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    @EntityGraph(attributePaths = {"recipientUser"})
    List<Notification> findAllByRecipientUser_IdOrderByCreatedAtDesc(UUID userId);
    @EntityGraph(attributePaths = {"recipientUser"})
    List<Notification> findAllByRecipientUser_IdAndReadAtIsNullOrderByCreatedAtDesc(UUID userId);
    long countByRecipientUser_IdAndReadAtIsNull(UUID userId);
}
