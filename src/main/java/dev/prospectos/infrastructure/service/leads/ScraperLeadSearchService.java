package dev.prospectos.infrastructure.service.leads;

import dev.prospectos.ai.client.ScraperClientInterface;
import dev.prospectos.api.CompanyDataService;
import dev.prospectos.api.LeadSearchService;
import dev.prospectos.api.SourceProvenanceService;
import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.api.dto.LeadResultDTO;
import dev.prospectos.api.dto.LeadSearchRequest;
import dev.prospectos.api.dto.LeadSearchResponse;
import dev.prospectos.api.dto.LeadSearchStatus;
import dev.prospectos.api.dto.SourceProvenanceDTO;
import dev.prospectos.api.dto.request.CompanyCreateRequest;
import dev.prospectos.core.domain.Website;
import dev.prospectos.core.enrichment.CompanyEnrichmentService;
import dev.prospectos.core.enrichment.EnrichmentRequest;
import dev.prospectos.core.enrichment.EnrichmentResult;
import dev.prospectos.core.enrichment.ScraperDataMapper;
import dev.prospectos.infrastructure.service.compliance.AllowedSourcesComplianceService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Profile("development")
public class ScraperLeadSearchService implements LeadSearchService {

    private static final int DEFAULT_LIMIT = 10;

    private final ScraperClientInterface scraperClient;
    private final CompanyEnrichmentService enrichmentService;
    private final CompanyDataService companyDataService;
    private final SourceProvenanceService sourceProvenanceService;
    private final AllowedSourcesComplianceService complianceService;

    public ScraperLeadSearchService(
        ScraperClientInterface scraperClient,
        CompanyEnrichmentService enrichmentService,
        CompanyDataService companyDataService,
        SourceProvenanceService sourceProvenanceService,
        AllowedSourcesComplianceService complianceService
    ) {
        this.scraperClient = scraperClient;
        this.enrichmentService = enrichmentService;
        this.companyDataService = companyDataService;
        this.sourceProvenanceService = sourceProvenanceService;
        this.complianceService = complianceService;
    }

    @Override
    public LeadSearchResponse searchLeads(LeadSearchRequest request) {
        if (request == null || request.query() == null || request.query().isBlank()) {
            throw new IllegalArgumentException("Query cannot be null or blank");
        }

        int limit = request.limit() == null ? DEFAULT_LIMIT : request.limit();
        List<String> requestedSources = complianceService.validateSources(request.sources());
        String sourceName = resolveSourceName(requestedSources);
        String query = request.query().trim();

        ScraperClientInterface.ScrapingResponse response = scraperClient.scrapeWebsiteSync(query, false);
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

        CompanyDTO company = persistCompany(enrichmentResult, query);
        if (company == null || limit <= 0) {
            return new LeadSearchResponse(
                LeadSearchStatus.COMPLETED,
                List.of(),
                UUID.randomUUID(),
                "No leads found"
            );
        }

        SourceProvenanceDTO provenance = new SourceProvenanceDTO(
            sourceName,
            resolveSourceUrl(enrichmentResult, query),
            Instant.now()
        );
        sourceProvenanceService.record(company, provenance);

        LeadResultDTO lead = new LeadResultDTO(company, null, provenance);

        return new LeadSearchResponse(
            LeadSearchStatus.COMPLETED,
            List.of(lead),
            UUID.randomUUID(),
            "Scraper search completed"
        );
    }

    private CompanyDTO persistCompany(EnrichmentResult enrichmentResult, String fallbackQuery) {
        String name = enrichmentResult.normalizedCompanyName();
        if (name == null || name.isBlank()) {
            name = fallbackQuery;
        }

        String industry = enrichmentResult.standardizedIndustry();
        String website = resolveSourceUrl(enrichmentResult, fallbackQuery);

        if (!isValidWebsite(website)) {
            return null;
        }

        String description = enrichmentResult.cleanDescription();
        String size = enrichmentResult.size() != null ? enrichmentResult.size().name() : null;

        CompanyCreateRequest createRequest = new CompanyCreateRequest(
            name.trim(),
            industry == null ? "Other" : industry.trim(),
            website,
            description,
            null,
            null,
            size
        );

        return companyDataService.createCompany(createRequest);
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
}
