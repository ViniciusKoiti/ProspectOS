package dev.prospectos.infrastructure.adapter;

import dev.prospectos.core.domain.Company;
import dev.prospectos.core.repository.CompanyDomainRepository;
import dev.prospectos.infrastructure.jpa.CompanyJpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter that bridges domain repository interface with JPA implementation.
 * Follows Adapter Pattern to keep domain independent of infrastructure.
 */
@Repository
public class CompanyRepositoryAdapter implements CompanyDomainRepository {
    
    private final CompanyJpaRepository jpaRepository;
    
    public CompanyRepositoryAdapter(CompanyJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public Company save(Company company) {
        return jpaRepository.save(company);
    }
    
    @Override
    public Optional<Company> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Company> findByExternalId(Long externalId) {
        return jpaRepository.findByExternalId(externalId);
    }
    
    @Override
    public void delete(Company company) {
        jpaRepository.delete(company);
    }
    
    @Override
    public List<Company> findAll() {
        return jpaRepository.findAll();
    }
    
    @Override
    public Optional<Company> findByWebsiteUrl(String websiteUrl) {
        return jpaRepository.findByWebsiteUrl(websiteUrl);
    }
    
    @Override
    public List<Company> findByIndustry(String industry) {
        return jpaRepository.findByIndustry(industry);
    }
    
    @Override
    public List<Company> findByStatus(Company.ProspectingStatus status) {
        return jpaRepository.findByStatus(status);
    }
    
    @Override
    public List<Company> findByMinScore(double minScore) {
        return jpaRepository.findByMinScore(minScore);
    }
    
    @Override
    public List<Company> findByScoreAndStatus(double minScore, Company.ProspectingStatus status) {
        return jpaRepository.findByScoreAndStatus(minScore, status);
    }
    
    @Override
    public List<Company> findTopQualifiedProspects() {
        return jpaRepository.findTopQualifiedProspects();
    }
    
    @Override
    public List<Company> findByIndustryAndSize(String industry, Company.CompanySize size) {
        return jpaRepository.findByIndustryAndSize(industry, size);
    }
    
    @Override
    public List<Company> findByCountryAndMinScore(String country, double minScore) {
        return jpaRepository.findByCountryAndMinScore(country, minScore);
    }
    
    @Override
    public List<Company> findRecentlyUpdated(Instant since) {
        return jpaRepository.findRecentlyUpdatedProspects(since);
    }
    
    @Override
    public List<Company> findRecentlyAdded(Instant since) {
        return jpaRepository.findRecentlyAdded(since);
    }
    
    @Override
    public long countByStatus(Company.ProspectingStatus status) {
        return jpaRepository.countByStatus(status);
    }
    
    @Override
    public Double getAverageScoreByIndustry(String industry) {
        return jpaRepository.getAverageScoreByIndustry(industry);
    }
    
    @Override
    public List<Company> findCompaniesWithoutContacts() {
        return jpaRepository.findCompaniesWithoutContacts();
    }
    
    @Override
    public List<Company> findCompaniesWithTechnologySignals() {
        return jpaRepository.findCompaniesWithTechnologySignals();
    }
    
    @Override
    public List<Company> findByWebsiteDomain(String domain) {
        return jpaRepository.findByWebsiteDomain(domain);
    }
    
    @Override
    public List<Company> findStaleProspects(double maxScore, Instant olderThan) {
        return jpaRepository.findStaleProspects(maxScore, olderThan);
    }
}
