package dev.prospectos.infrastructure.adapter;

import dev.prospectos.core.domain.ICP;
import dev.prospectos.core.repository.ICPDomainRepository;
import dev.prospectos.infrastructure.jpa.ICPJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter that bridges ICP domain repository interface with JPA implementation.
 */
@Repository
public class ICPRepositoryAdapter implements ICPDomainRepository {
    
    private final ICPJpaRepository jpaRepository;
    
    public ICPRepositoryAdapter(ICPJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public ICP save(ICP icp) {
        return jpaRepository.save(icp);
    }
    
    @Override
    public Optional<ICP> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<ICP> findByExternalId(Long externalId) {
        return jpaRepository.findByExternalId(externalId);
    }
    
    @Override
    public void delete(ICP icp) {
        jpaRepository.delete(icp);
    }
    
    @Override
    public List<ICP> findAll() {
        return jpaRepository.findAll();
    }
    
    @Override
    public List<ICP> findByIndustry(String industry) {
        return jpaRepository.findByIndustry(industry);
    }
    
    @Override
    public List<ICP> findByTargetRole(String role) {
        return jpaRepository.findByTargetRole(role);
    }
    
    @Override
    public Optional<ICP> findByName(String name) {
        return jpaRepository.findByName(name);
    }
    
    @Override
    public List<ICP> findActiveICPs() {
        return jpaRepository.findActiveICPs();
    }
    
    @Override
    public ICP findDefaultICP() {
        return jpaRepository.findDefaultICP();
    }
}
