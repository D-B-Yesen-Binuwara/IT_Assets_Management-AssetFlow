package com.binuwara.AssetsFlow.Repository;

import com.binuwara.AssetsFlow.Entity.OrganizationSettings;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganizationSettingsRepository extends JpaRepository<OrganizationSettings, Short> {
    @EntityGraph(attributePaths = {"updatedBy"})
    Optional<OrganizationSettings> findById(Short id);
}
