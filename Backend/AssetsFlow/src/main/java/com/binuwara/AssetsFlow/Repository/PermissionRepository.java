package com.binuwara.AssetsFlow.Repository;

import com.binuwara.AssetsFlow.Entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {
    List<Permission> findAllByOrderByCodeAsc();
    Optional<Permission> findByCodeIgnoreCase(String code);
}
