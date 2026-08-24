package com.binuwara.AssetsFlow.Repository;

import com.binuwara.AssetsFlow.Entity.LicenseAssignment;
import com.binuwara.AssetsFlow.Entity.LicenseAssignmentStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LicenseAssignmentRepository extends JpaRepository<LicenseAssignment, UUID> {
    @EntityGraph(attributePaths = {"license", "license.vendor", "employee", "employee.department"})
    List<LicenseAssignment> findAllByOrderByAssignedAtDesc();
    @EntityGraph(attributePaths = {"license", "employee", "employee.department"})
    Optional<LicenseAssignment> findWithReferencesById(UUID id);
    List<LicenseAssignment> findAllByLicense_IdOrderByAssignedAtDesc(UUID licenseId);
    boolean existsByLicense_IdAndEmployee_IdAndStatus(UUID licenseId, UUID employeeId, LicenseAssignmentStatus status);
    long countByLicense_IdAndStatus(UUID licenseId, LicenseAssignmentStatus status);
}
