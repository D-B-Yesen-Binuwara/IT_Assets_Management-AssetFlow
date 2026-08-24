package com.binuwara.AssetsFlow.Repository;

import com.binuwara.AssetsFlow.Entity.AssetLifecycleEvent;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AssetLifecycleEventRepository extends JpaRepository<AssetLifecycleEvent, UUID> {
    @EntityGraph(attributePaths = {"asset", "actorUser", "fromLocation", "toLocation", "assignment"})
    List<AssetLifecycleEvent> findAllByAsset_IdOrderByEventAtDesc(UUID assetId);
    @EntityGraph(attributePaths = {"asset", "actorUser", "fromLocation", "toLocation", "assignment"})
    List<AssetLifecycleEvent> findTop100ByOrderByEventAtDesc();
}
