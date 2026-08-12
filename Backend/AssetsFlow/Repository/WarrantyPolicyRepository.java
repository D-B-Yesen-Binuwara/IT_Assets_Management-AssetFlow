package com.binuwara.AssetsFlow.Repository;

import com.binuwara.AssetsFlow.Entity.WarrantyPolicy;
import com.binuwara.AssetsFlow.Entity.WarrantyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WarrantyPolicyRepository extends JpaRepository<WarrantyPolicy, UUID> {
    List<WarrantyPolicy> findAllByOrderByEndDateAsc();
    Optional<WarrantyPolicy> findByAsset_Id(UUID assetId);
    boolean existsByAsset_Id(UUID assetId);
    long countByStatus(WarrantyStatus status);
    long countByEndDateBetween(LocalDate from, LocalDate to);
}
