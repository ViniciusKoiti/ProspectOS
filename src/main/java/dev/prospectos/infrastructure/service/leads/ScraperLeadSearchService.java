package dev.prospectos.infrastructure.service.leads;

import dev.prospectos.ai.client.ScraperClientInterface;
import dev.prospectos.ai.client.ScrapingResponse;
import dev.prospectos.api.ICPDataService;
import dev.prospectos.api.LeadSearchService;
import dev.prospectos.api.dto.CompanyCandidateDTO;
import dev.prospectos.api.dto.ICPDto;
import dev.prospectos.api.dto.LeadResultDTO;
import dev.prospectos.api.dto.LeadSearchRequest;
import dev.prospectos.api.dto.LeadSearchResponse;
import dev.prospectos.api.dto.LeadSearchStatus;
import dev.prospectos.api.dto.ScoreDTO;
import dev.prospectos.api.dto.SourceProvenanceDTO;
import dev.prospectos.core.domain.Company;
import dev.prospectos.core.domain.ICP;
import dev.prospectos.core.domain.Website;
import dev.prospectos.core.enrichment.CompanyEnrichmentService;
import dev.prospectos.core.enrichment.EnrichmentRequest;
import dev.prospectos.core.enrichment.EnrichmentResult;
import dev.prospectos.core.enrichment.ScraperDataMapper;
import dev.prospectos.core.util.LeadKeyGenerator;
import dev.prospectos.infrastructure.config.LeadSearchProperties;
import dev.prospectos.infrastructure.service.compliance.AllowedSourcesComplianceService;
import dev.prospectos.infrastructure.service.scoring.CompanyScoringService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Profile("development")
public class ScraperLeadSearchService implements LeadSearchService {

    private static final int DEFAULT_LIMIT = 10;

    private final ScraperClientInterface scraperClient;
    private final CompanyEnrichmentService enrichmentService;
    private final ICPDataService icpDataService;
    private final CompanyScoringService scoringService;
    private final AllowedSourcesComplianceService complianceService;
    private final LeadSearchProperties properties;

    public ScraperLeadSearchService(
        ScraperClientInterface scraperClient,
        CompanyEnrichmentService enrichmentService,
        ICPDataService icpDataService,
        CompanyScoringService scoringService,
        AllowedSourcesComplianceService complianceService,
        LeadSearchProperties properties
    ) {
        this.scraperClient = scraperClient;
        this.enrichmentService = enrichmentService;
        this.icpDataService = icpDataService;
        this.scoringService = scoringService;
        this.complianceService = complianceService;
        this.properties = properties;
    }

    @Override
    public LeadSearchResponse searchLeads(LeadSearchRequest request) {
        if (request == null || request.query() == null || request.query().isBlank()) {
            throw new IllegalArgumentException("Query cannot be null or blank");
        }

        int limit = request.limit() == null ? DEFAULT_LIMIT : request.limit();
        List<String> requestedSources = complianceService.validateSources(request.sources());
        String sourceName = resolveSourceName(requestedSources);
        String query = normalizeWebsiteOrQuery(request.query());

        ScrapingResponse response = scraperClient.scrapeWebsiteSync(query, false);
        if (!response.success() || response.data() == null) {
            return new LeadSearchResponse(
                LeadSearchStatus.FAILED,
                List.of(),
                UUID.randomUUID(),
                response.error() == null ? "Scraper failed" : response.error()
            );
        }

        EnrichmentRequest enrichmentRequest = ScraperDataMapper.fromScraperData(response.data(), query);
        EnrichmentResult enrichmentResult = enrichmentService.enrichCompanyData(enrichmentRequest);

        Company candidateCompany = buildCandidateCompany(enrichmentResult, query);
        if (candidateCompany == null || limit <= 0) {
            return new LeadSearchResponse(
                LeadSearchStatus.COMPLETED,
                List.of(),
                UUID.randomUUID(),
                "No leads found"
            );
        }

        Long icpId = resolveIcpId(request.icpId());
        ICPDto icpDto = icpDataService.findICP(icpId);
        if (icpDto == null) {
            throw new IllegalArgumentException("ICP not found with id: " + icpId);
        }

        ICP icp = toDomainICP(icpDto);
        ScoreDTO score = scoringService.scoreCandidate(candidateCompany, icp);

        CompanyCandidateDTO candidate = toCompanyCandidateDTO(candidateCompany, enrichmentResult);
        String websiteUrl = resolveSourceUrl(enrichmentResult, query);
        String leadKey = LeadKeyGenerator.generate(websiteUrl, sourceName);

        SourceProvenanceDTO provenance = new SourceProvenanceDTO(
            sourceName,
            websiteUrl,
            Instant.now()
        );

        LeadResultDTO lead = new LeadResultDTO(candidate, score, provenance, leadKey);

        return new LeadSearchResponse(
            LeadSearchStatus.COMPLETED,
            List.of(lead),
            UUID.randomUUID(),
            "Scraper search completed"
        );
    }

    private Company buildCandidateCompany(EnrichmentResult enrichmentResult, String fallbackQuery) {
        String name = enrichmentResult.normalizedCompanyName();
        if (name == null || name.isBlank()) {
            name = fallbackQuery;
        }

        String industry = enrichmentResult.standardizedIndustry();
        String websiteUrl = resolveSourceUrl(enrichmentResult, fallbackQuery);

        if (!isValidWebsite(websiteUrl)) {
            return null;
        }

        Website website = Website.of(websiteUrl);
        String description = enrichmentResult.cleanDescription();
        Company.CompanySize size = enrichmentResult.size();

        Company company = Company.create(
            name.trim(),
            website,
            industry == null ? "Other" : industry.trim()
        );
        
        if (description != null) {
            company.setDescription(description);
        }
        
        if (size != null) {
            company.setSize(size);
        }
        
        return company;
    }

    private CompanyCandidateDTO toCompanyCandidateDTO(Company company, EnrichmentResult enrichmentResult) {
        List<String> contacts = enrichmentResult.validatedContacts() != null
            ? enrichmentResult.validatedContacts().stream()
                .filter(c -> c.isUsable())
                .map(c -> c.getEmail())
                .collect(Collectors.toList())
            : List.of();

        return new CompanyCandidateDTO(
            company.getName(),
            company.getWebsite().getUrl(),
            company.getIndustry(),
            company.getDescription(),
            company.getSize() != null ? company.getSize().name() : null,
            null, // location not available from scraper
            contacts
        );
    }

    private Long resolveIcpId(Long requestIcpId) {
        if (requestIcpId != null) {
            return requestIcpId;
        }
        if (properties.defaultIcpId() == null) {
            throw new IllegalArgumentException("ICP ID is required. Provide icpId in request or configure prospectos.leads.default-icp-id");
        }
        return properties.defaultIcpId();
    }

    private ICP toDomainICP(ICPDto icpDTO) {
        return ICP.create(
            icpDTO.name(),
            icpDTO.description(),
            icpDTO.targetIndustries() != null ? icpDTO.targetIndustries() : List.of(),
            icpDTO.regions() != null ? icpDTO.regions() : List.of(),
            icpDTO.targetRoles() != null ? icpDTO.targetRoles() : List.of(),
            icpDTO.interestTheme()
        );
    }

    private boolean isValidWebsite(String website) {
        try {
            Website.of(website);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private String resolveSourceUrl(EnrichmentResult enrichmentResult, String fallbackQuery) {
        if (enrichmentResult.website() != null) {
            return enrichmentResult.website().getUrl();
        }
        return fallbackQuery;
    }

    private String resolveSourceName(List<String> sources) {
        if (sources == null || sources.isEmpty()) {
            return "scraper";
        }
        return sources.get(0);
    }

    private String normalizeWebsiteOrQuery(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return trimmed;
        }
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }
        if (trimmed.contains(" ")) {
            return trimmed;
        }
        return "https://" + trimmed;
    }
}
