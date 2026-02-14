package dev.prospectos.core.repository;

import dev.prospectos.core.domain.ICP;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Pure domain repository interface for ICP (Ideal Customer Profile) aggregate.
 * Contains only business methods without infrastructure dependencies.
 */
public interface ICPDomainRepository {
    
    // Basic CRUD operations
    ICP save(ICP icp);
    Optional<ICP> findById(UUID id);
    Optional<ICP> findByExternalId(Long externalId);
    void delete(ICP icp);
    List<ICP> findAll();
    
    // Business queries
    List<ICP> findByIndustry(String industry);
    List<ICP> findByTargetRole(String role);
    Optional<ICP> findByName(String name);
    
    // Active ICP management
    List<ICP> findActiveICPs();
    ICP findDefaultICP();
}
