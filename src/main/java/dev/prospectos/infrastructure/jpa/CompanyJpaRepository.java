package dev.prospectos.infrastructure.jpa;

import dev.prospectos.core.domain.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA repository interface for Company aggregate.
 * Contains JPA-specific implementations and queries.
 */
public interface CompanyJpaRepository extends JpaRepository<Company, UUID> {
    
    Optional<Company> findByWebsiteUrl(String websiteUrl);
    
    List<Company> findByIndustry(String industry);
    
    List<Company> findByStatus(Company.ProspectingStatus status);
    
    @Query("SELECT c FROM Company c WHERE c.prospectingScore.value >= :minScore ORDER BY c.prospectingScore.value DESC")
    List<Company> findByMinScore(@Param("minScore") double minScore);
    
    @Query("SELECT c FROM Company c WHERE c.prospectingScore.value >= :minScore AND c.status = :status")
    List<Company> findByScoreAndStatus(@Param("minScore") double minScore, @Param("status") Company.ProspectingStatus status);
    
    @Query("SELECT c FROM Company c WHERE c.status = 'QUALIFIED' AND c.prospectingScore.value >= 80.0 ORDER BY c.prospectingScore.value DESC")
    List<Company> findTopQualifiedProspects();
    
    @Query("SELECT c FROM Company c WHERE c.industry = :industry AND c.size = :size")
    List<Company> findByIndustryAndSize(@Param("industry") String industry, @Param("size") Company.CompanySize size);
    
    @Query("SELECT c FROM Company c WHERE c.country = :country AND c.prospectingScore.value >= :minScore")
    List<Company> findByCountryAndMinScore(@Param("country") String country, @Param("minScore") double minScore);

    @Query("SELECT c FROM Company c WHERE c.lastUpdatedAt >= :since AND c.status IN ('QUALIFIED', 'OPPORTUNITY') ORDER BY c.lastUpdatedAt DESC")
    List<Company> findRecentlyUpdatedProspects(@Param("since") Instant since);

    @Query("SELECT c FROM Company c WHERE c.createdAt >= :since ORDER BY c.createdAt DESC")
    List<Company> findRecentlyAdded(@Param("since") Instant since);

    @Query("SELECT COUNT(c) FROM Company c WHERE c.status = :status")
    long countByStatus(@Param("status") Company.ProspectingStatus status);
    
    @Query("SELECT AVG(c.prospectingScore.value) FROM Company c WHERE c.industry = :industry")
    Double getAverageScoreByIndustry(@Param("industry") String industry);
    
    @Query("SELECT c FROM Company c WHERE SIZE(c.contacts) = 0")
    List<Company> findCompaniesWithoutContacts();
    
    @Query("SELECT c FROM Company c WHERE SIZE(c.technologySignals) > 0 ORDER BY c.lastUpdatedAt DESC")
    List<Company> findCompaniesWithTechnologySignals();
    
    @Query("SELECT c FROM Company c WHERE c.website.domain = :domain")
    List<Company> findByWebsiteDomain(@Param("domain") String domain);
    
    @Query("""
        SELECT c FROM Company c 
        WHERE c.status IN ('NEW', 'REVIEWING') 
        AND c.prospectingScore.value < :maxScore 
        AND c.createdAt < :olderThan
        ORDER BY c.createdAt ASC
        """)
    List<Company> findStaleProspects(@Param("maxScore") double maxScore, @Param("olderThan") Instant olderThan);
}