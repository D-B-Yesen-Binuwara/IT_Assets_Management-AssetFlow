package com.binuwara.AssetsFlow.Repository;

import com.binuwara.AssetsFlow.Entity.EmployeeDepartmentHistory;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EmployeeDepartmentHistoryRepository extends JpaRepository<EmployeeDepartmentHistory, UUID> {
    @EntityGraph(attributePaths = {"employee", "previousDepartment", "newDepartment", "changedByUser"})
    List<EmployeeDepartmentHistory> findAllByEmployee_IdOrderByEffectiveFromDesc(UUID employeeId);
}
