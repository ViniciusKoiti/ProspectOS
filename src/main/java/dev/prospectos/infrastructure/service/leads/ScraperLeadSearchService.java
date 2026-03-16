package dev.prospectos.infrastructure.service.leads;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import dev.prospectos.ai.client.ScraperClientInterface;
import dev.prospectos.ai.client.ScrapingResponse;
import dev.prospectos.api.CompanyDataService;
import dev.prospectos.api.ICPDataService;
import dev.prospectos.api.LeadDiscoveryService;
import dev.prospectos.api.LeadSearchService;
import dev.prospectos.api.dto.CompanyCandidateDTO;
import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.api.dto.ICPDto;
import dev.prospectos.api.dto.LeadDiscoveryRequest;
import dev.prospectos.api.dto.LeadResultDTO;
import dev.prospectos.api.dto.LeadSearchRequest;
import dev.prospectos.api.dto.LeadSearchResponse;
import dev.prospectos.api.dto.LeadSearchStatus;
import dev.prospectos.api.dto.ScoreDTO;
import dev.prospectos.api.dto.SourceProvenanceDTO;
import dev.prospectos.core.domain.Company;
import dev.prospectos.core.domain.ICP;
import dev.prospectos.core.enrichment.CompanyEnrichmentService;
import dev.prospectos.core.enrichment.EnrichmentRequest;
import dev.prospectos.core.enrichment.EnrichmentResult;
import dev.prospectos.core.enrichment.ScraperDataMapper;
import dev.prospectos.core.util.LeadKeyGenerator;
import dev.prospectos.infrastructure.config.LeadSearchProperties;
import dev.prospectos.infrastructure.service.compliance.AllowedSourcesComplianceService;
import dev.prospectos.infrastructure.service.inmemory.InMemoryLeadQueryMatcher;
import dev.prospectos.infrastructure.service.inmemory.InMemoryLeadResultFactory;
import dev.prospectos.infrastructure.service.scoring.CompanyScoringService;

@Service
@Profile("development")
public class ScraperLeadSearchService implements LeadSearchService {

    private static final Set<String> DISCOVERY_SOURCES = Set.of("vector-company", "cnpj-ws", "llm-discovery");

    private final ScraperClientInterface scraperClient;
    private final CompanyEnrichmentService enrichmentService;
    private final CompanyDataService companyDataService;
    private final LeadDiscoveryService leadDiscoveryService;
    private final ICPDataService icpDataService;
    private final CompanyScoringService scoringService;
    private final ScraperLeadRequestResolver requestResolver;
    private final ScraperLeadCandidateFactory candidateFactory = new ScraperLeadCandidateFactory();
    private final ScraperLeadResponseFactory responseFactory = new ScraperLeadResponseFactory();
    private final InMemoryLeadResultFactory inMemoryLeadResultFactory;

    public ScraperLeadSearchService(
        ScraperClientInterface scraperClient,
        CompanyEnrichmentService enrichmentService,
        CompanyDataService companyDataService,
        LeadDiscoveryService leadDiscoveryService,
        ICPDataService icpDataService,
        CompanyScoringService scoringService,
        AllowedSourcesComplianceService complianceService,
        LeadSearchProperties properties
    ) {
        this.scraperClient = scraperClient;
        this.enrichmentService = enrichmentService;
        this.companyDataService = companyDataService;
        this.leadDiscoveryService = leadDiscoveryService;
        this.icpDataService = icpDataService;
        this.scoringService = scoringService;
        this.requestResolver = new ScraperLeadRequestResolver(complianceService, properties);
        this.inMemoryLeadResultFactory = new InMemoryLeadResultFactory(scoringService);
    }

    @Override
    public LeadSearchResponse searchLeads(LeadSearchRequest request) {
        ScraperLeadRequestContext context = requestResolver.resolve(request);
        Long icpId = requestResolver.resolveIcpId(request.icpId());
        ICP icp = loadIcp(icpId);

        List<String> failures = new ArrayList<>();
        List<LeadResultDTO> aggregated = new ArrayList<>();

        for (String source : context.sources()) {
            try {
                aggregated.addAll(searchBySource(source, context, icpId, icp));
            } catch (RuntimeException exception) {
                failures.add(source + ": " + messageOrFallback(exception, "Source search failed"));
            }
        }

        List<LeadResultDTO> leads = deduplicateAndLimit(aggregated, context.limit());
        if (!leads.isEmpty()) {
            String message = failures.isEmpty()
                ? "Search completed"
                : "Search completed with partial failures: " + String.join(" | ", failures);
            return responseFactory.completed(leads, message);
        }

        if (!failures.isEmpty()) {
            return responseFactory.failed(String.join(" | ", failures));
        }

        return responseFactory.noLeads();
    }

    private List<LeadResultDTO> searchBySource(
        String source,
        ScraperLeadRequestContext context,
        Long icpId,
        ICP icp
    ) {
        if ("in-memory".equals(source)) {
            return searchInMemory(source, context, icp);
        }

        if (DISCOVERY_SOURCES.contains(source)) {
            return searchDiscovery(source, context, icpId);
        }

        return searchScraper(source, context, icp);
    }

    private List<LeadResultDTO> searchInMemory(
        String source,
        ScraperLeadRequestContext context,
        ICP icp
    ) {
        List<String> tokens = InMemoryLeadQueryMatcher.tokens(context.query());
        List<CompanyDTO> companies = companyDataService.findAllCompanies();

        return companies.stream()
            .filter(company -> InMemoryLeadQueryMatcher.matches(company, tokens))
            .limit(context.limit())
            .map(company -> inMemoryLeadResultFactory.toLeadResult(company, source, icp))
            .toList();
    }

    private List<LeadResultDTO> searchDiscovery(
        String source,
        ScraperLeadRequestContext context,
        Long icpId
    ) {
        LeadDiscoveryRequest discoveryRequest = new LeadDiscoveryRequest(
            context.query(),
            null,
            context.limit(),
            List.of(source),
            icpId
        );

        LeadSearchResponse response = leadDiscoveryService.discoverLeads(discoveryRequest);
        if (response.status() == LeadSearchStatus.FAILED) {
            throw new IllegalStateException(response.message() == null ? "Discovery failed" : response.message());
        }

        return response.leads();
    }

    private List<LeadResultDTO> searchScraper(
        String source,
        ScraperLeadRequestContext context,
        ICP icp
    ) {
        ScrapingResponse scrapingResponse = scraperClient.scrapeWebsiteSync(context.scraperQuery(), false);
        if (!scrapingResponse.success() || scrapingResponse.data() == null) {
            throw new IllegalStateException(scrapingResponse.error() == null ? "Scraper failed" : scrapingResponse.error());
        }

        EnrichmentRequest enrichmentRequest = ScraperDataMapper.fromScraperData(scrapingResponse.data(), context.scraperQuery());
        EnrichmentResult enrichmentResult = enrichmentService.enrichCompanyData(enrichmentRequest);
        Company candidateCompany = candidateFactory.buildCandidateCompany(enrichmentResult, context.scraperQuery());
        if (candidateCompany == null || context.limit() <= 0) {
            return List.of();
        }

        ScoreDTO score = scoringService.scoreCandidate(candidateCompany, icp);
        CompanyCandidateDTO candidate = candidateFactory.toCompanyCandidateDTO(candidateCompany, enrichmentResult);
        String sourceUrl = candidateFactory.resolveSourceUrl(enrichmentResult, context.scraperQuery());
        String leadKey = LeadKeyGenerator.generate(sourceUrl, source);
        SourceProvenanceDTO provenance = new SourceProvenanceDTO(source, sourceUrl, Instant.now());

        return List.of(new LeadResultDTO(candidate, score, provenance, leadKey));
    }

    private List<LeadResultDTO> deduplicateAndLimit(List<LeadResultDTO> leads, int limit) {
        Map<String, LeadResultDTO> uniqueByLeadKey = new LinkedHashMap<>();
        for (LeadResultDTO lead : leads) {
            if (lead == null || lead.leadKey() == null || lead.leadKey().isBlank()) {
                continue;
            }
            uniqueByLeadKey.putIfAbsent(lead.leadKey(), lead);
        }

        return uniqueByLeadKey.values().stream()
            .limit(limit)
            .toList();
    }

    private ICP loadIcp(Long icpId) {
        ICPDto icpDto = icpDataService.findICP(icpId);
        if (icpDto == null) {
            throw new IllegalArgumentException("ICP not found with id: " + icpId);
        }
        return ScraperLeadIcpMapper.toDomainICP(icpDto);
    }

    private String messageOrFallback(RuntimeException exception, String fallback) {
        if (exception == null || exception.getMessage() == null || exception.getMessage().isBlank()) {
            return fallback;
        }
        return exception.getMessage();
    }
}
