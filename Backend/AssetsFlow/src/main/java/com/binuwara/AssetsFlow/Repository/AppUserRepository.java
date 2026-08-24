package com.binuwara.AssetsFlow.Repository;

import com.binuwara.AssetsFlow.Entity.AppUser;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCaseAndIdNot(String username, UUID id);

    @EntityGraph(attributePaths = {"employee", "employee.department", "roles"})
    Optional<AppUser> findByUsernameIgnoreCase(String username);

    @EntityGraph(attributePaths = {"employee", "employee.department", "roles"})
    Optional<AppUser> findByEmployee_Id(UUID employeeId);

    @EntityGraph(attributePaths = {"employee", "employee.department", "roles"})
    Optional<AppUser> findWithIdentityById(UUID id);

    @EntityGraph(attributePaths = {"employee", "employee.department", "roles"})
    List<AppUser> findAllByOrderByCreatedAtDesc();
}
