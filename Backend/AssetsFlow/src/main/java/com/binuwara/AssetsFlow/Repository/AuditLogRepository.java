package com.binuwara.AssetsFlow.Repository;

import com.binuwara.AssetsFlow.Entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
}
