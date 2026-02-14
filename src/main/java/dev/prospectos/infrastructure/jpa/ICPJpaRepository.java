package dev.prospectos.infrastructure.jpa;

import dev.prospectos.core.domain.ICP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA repository interface for ICP (Ideal Customer Profile) aggregate.
 */
public interface ICPJpaRepository extends JpaRepository<ICP, UUID> {
    
    @Query("SELECT i FROM ICP i JOIN i.industries industry WHERE industry = :industry")
    List<ICP> findByIndustry(@Param("industry") String industry);
    
    @Query("SELECT i FROM ICP i JOIN i.targetRoles role WHERE role = :role")
    List<ICP> findByTargetRole(@Param("role") String role);
    
    Optional<ICP> findByName(String name);
    
    @Query("SELECT i FROM ICP i")
    List<ICP> findActiveICPs();
    
    @Query("SELECT i FROM ICP i WHERE i.name = 'Default'")
    ICP findDefaultICP();

    Optional<ICP> findByExternalId(Long externalId);
}
