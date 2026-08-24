package com.binuwara.AssetsFlow.Repository;

import com.binuwara.AssetsFlow.Entity.Assignment;
import com.binuwara.AssetsFlow.Entity.AssignmentStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssignmentRepository extends JpaRepository<Assignment, UUID> {
    @EntityGraph(attributePaths = {"asset", "asset.category", "asset.location", "employee", "employee.department"})
    List<Assignment> findAllByOrderByAssignedAtDesc();

    @EntityGraph(attributePaths = {"asset", "asset.category", "asset.location", "employee", "employee.department"})
    Optional<Assignment> findWithReferencesById(UUID id);

    @EntityGraph(attributePaths = {"asset", "asset.category", "asset.location", "employee", "employee.department"})
    Optional<Assignment> findByAsset_IdAndStatus(UUID assetId, AssignmentStatus status);

    List<Assignment> findAllByAsset_IdOrderByAssignedAtDesc(UUID assetId);
    boolean existsByAsset_IdAndStatus(UUID assetId, AssignmentStatus status);
    long countByStatus(AssignmentStatus status);
}
