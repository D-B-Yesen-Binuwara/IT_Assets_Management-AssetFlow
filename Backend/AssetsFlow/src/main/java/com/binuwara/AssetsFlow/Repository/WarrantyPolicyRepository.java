package com.binuwara.AssetsFlow.Repository;

import com.binuwara.AssetsFlow.Entity.WarrantyPolicy;
import com.binuwara.AssetsFlow.Entity.WarrantyStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WarrantyPolicyRepository extends JpaRepository<WarrantyPolicy, UUID> {
    @EntityGraph(attributePaths = {"asset", "asset.category", "vendor"})
    List<WarrantyPolicy> findAllByOrderByEndDateAsc();
    @EntityGraph(attributePaths = {"asset", "asset.category", "vendor"})
    Optional<WarrantyPolicy> findWithReferencesById(UUID id);
    Optional<WarrantyPolicy> findByAsset_IdAndCurrentTrue(UUID assetId);
    List<WarrantyPolicy> findAllByAsset_IdOrderByStartDateDesc(UUID assetId);
    boolean existsByAsset_IdAndCurrentTrue(UUID assetId);
    long countByStatus(WarrantyStatus status);
    long countByEndDateBetween(LocalDate from, LocalDate to);
}
