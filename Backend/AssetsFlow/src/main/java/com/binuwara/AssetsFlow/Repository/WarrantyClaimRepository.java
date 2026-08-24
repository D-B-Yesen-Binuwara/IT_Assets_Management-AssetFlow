package com.binuwara.AssetsFlow.Repository;

import com.binuwara.AssetsFlow.Entity.WarrantyClaim;
import com.binuwara.AssetsFlow.Entity.WarrantyClaimStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WarrantyClaimRepository extends JpaRepository<WarrantyClaim, UUID> {
    @EntityGraph(attributePaths = {"warranty", "warranty.asset", "assignedToUser"})
    List<WarrantyClaim> findAllByOrderByOpenedAtDesc();
    @EntityGraph(attributePaths = {"warranty", "warranty.asset", "assignedToUser"})
    List<WarrantyClaim> findAllByWarranty_Asset_IdOrderByOpenedAtDesc(UUID assetId);
    Optional<WarrantyClaim> findByClaimNumberIgnoreCase(String claimNumber);
    boolean existsByClaimNumberIgnoreCase(String claimNumber);
    boolean existsByClaimNumberIgnoreCaseAndIdNot(String claimNumber, UUID id);
    long countByStatus(WarrantyClaimStatus status);
}
