package com.binuwara.AssetsFlow.Repository;

import com.binuwara.AssetsFlow.Entity.AssetValuation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssetValuationRepository extends JpaRepository<AssetValuation, UUID> {
    @EntityGraph(attributePaths = {"asset", "createdByUser"})
    List<AssetValuation> findAllByAsset_IdOrderByValuationDateDesc(UUID assetId);
    @EntityGraph(attributePaths = {"asset", "createdByUser"})
    Optional<AssetValuation> findByAsset_IdAndValuationDate(UUID assetId, LocalDate valuationDate);
    @EntityGraph(attributePaths = {"asset", "createdByUser"})
    Optional<AssetValuation> findFirstByAsset_IdOrderByValuationDateDesc(UUID assetId);
}
