package com.binuwara.AssetsFlow.Repository;

import com.binuwara.AssetsFlow.Entity.AssetDisposal;
import com.binuwara.AssetsFlow.Entity.AssetDisposalStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssetDisposalRepository extends JpaRepository<AssetDisposal, UUID> {
    @EntityGraph(attributePaths = {"asset", "approvedByUser"})
    List<AssetDisposal> findAllByOrderByDisposalDateDesc();
    @EntityGraph(attributePaths = {"asset", "approvedByUser"})
    Optional<AssetDisposal> findWithDetailsById(UUID id);
    Optional<AssetDisposal> findByAsset_Id(UUID assetId);
    long countByStatus(AssetDisposalStatus status);
}
