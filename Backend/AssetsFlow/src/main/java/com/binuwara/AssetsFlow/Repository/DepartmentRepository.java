package com.binuwara.AssetsFlow.Repository;

import com.binuwara.AssetsFlow.Entity.Department;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DepartmentRepository extends JpaRepository<Department, UUID> {
    @EntityGraph(attributePaths = {"manager"})
    List<Department> findAllByOrderByNameAsc();
    @EntityGraph(attributePaths = {"manager"})
    Optional<Department> findWithManagerById(UUID id);
    Optional<Department> findByCodeIgnoreCase(String code);
    Optional<Department> findByNameIgnoreCase(String name);
    boolean existsByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCaseAndIdNot(String code, UUID id);
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);
}
