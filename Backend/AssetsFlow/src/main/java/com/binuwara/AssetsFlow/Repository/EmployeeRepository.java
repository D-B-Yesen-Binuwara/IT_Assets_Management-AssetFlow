package com.binuwara.AssetsFlow.Repository;

import com.binuwara.AssetsFlow.Entity.Employee;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    Optional<Employee> findByEmailIgnoreCase(String email);

    Optional<Employee> findByEmployeeNumberIgnoreCase(String employeeNumber);

    @EntityGraph(attributePaths = {"department"})
    Optional<Employee> findWithDepartmentById(UUID id);

    @EntityGraph(attributePaths = {"department"})
    List<Employee> findAllByOrderByEmployeeNumberAsc();
}
