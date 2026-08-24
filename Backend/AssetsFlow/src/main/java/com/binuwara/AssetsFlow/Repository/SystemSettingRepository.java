package com.binuwara.AssetsFlow.Repository;

import com.binuwara.AssetsFlow.Entity.SystemSetting;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SystemSettingRepository extends JpaRepository<SystemSetting, String> {
    @EntityGraph(attributePaths = {"updatedBy"})
    List<SystemSetting> findAllByOrderBySettingKeyAsc();
}
