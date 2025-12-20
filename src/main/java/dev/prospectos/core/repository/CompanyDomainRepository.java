package dev.prospectos.core.repository;

import dev.prospectos.core.domain.Company;
import dev.prospectos.core.domain.Score;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Pure domain repository interface for Company aggregate.
 * Contains only business methods without infrastructure dependencies.
 */
public interface CompanyDomainRepository {
    
    // Basic CRUD operations
    Company save(Company company);
    Optional<Company> findById(UUID id);
    void delete(Company company);
    List<Company> findAll();
    
    // Business-focused queries
    Optional<Company> findByWebsiteUrl(String websiteUrl);
    List<Company> findByIndustry(String industry);
    List<Company> findByStatus(Company.ProspectingStatus status);
    
    // Prospecting queries
    List<Company> findByMinScore(double minScore);
    List<Company> findByScoreAndStatus(double minScore, Company.ProspectingStatus status);
    List<Company> findTopQualifiedProspects();
    List<Company> findByIndustryAndSize(String industry, Company.CompanySize size);
    List<Company> findByCountryAndMinScore(String country, double minScore);
    
    // Analytics queries
    List<Company> findRecentlyUpdated(Instant since);
    List<Company> findRecentlyAdded(Instant since);
    long countByStatus(Company.ProspectingStatus status);
    Double getAverageScoreByIndustry(String industry);
    
    // Signal-based queries
    List<Company> findCompaniesWithoutContacts();
    List<Company> findCompaniesWithTechnologySignals();
    List<Company> findByWebsiteDomain(String domain);
    List<Company> findStaleProspects(double maxScore, Instant olderThan);
}