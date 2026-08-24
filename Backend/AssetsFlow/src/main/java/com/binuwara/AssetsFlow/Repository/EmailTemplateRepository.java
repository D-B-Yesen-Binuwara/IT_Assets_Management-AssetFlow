package com.binuwara.AssetsFlow.Repository;

import com.binuwara.AssetsFlow.Entity.EmailTemplate;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, UUID> {
    @EntityGraph(attributePaths = {"updatedBy"})
    List<EmailTemplate> findAllByOrderByTemplateKeyAsc();

    Optional<EmailTemplate> findByTemplateKeyIgnoreCase(String templateKey);
    boolean existsByTemplateKeyIgnoreCase(String templateKey);
    boolean existsByTemplateKeyIgnoreCaseAndIdNot(String templateKey, UUID id);
}
