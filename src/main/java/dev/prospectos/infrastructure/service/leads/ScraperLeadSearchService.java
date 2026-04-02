package dev.prospectos.infrastructure.service.leads;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import dev.prospectos.ai.client.ScraperClientInterface;
import dev.prospectos.api.CompanyDataService;
import dev.prospectos.api.ICPDataService;
import dev.prospectos.api.LeadDiscoveryService;
import dev.prospectos.api.LeadSearchService;
import dev.prospectos.api.dto.LeadResultDTO;
import dev.prospectos.api.dto.LeadSearchRequest;
import dev.prospectos.api.dto.LeadSearchResponse;
import dev.prospectos.api.mcp.QueryMetricsRecorder;
import dev.prospectos.core.domain.ICP;
import dev.prospectos.core.enrichment.CompanyEnrichmentService;
import dev.prospectos.infrastructure.config.LeadSearchProperties;
import dev.prospectos.infrastructure.service.compliance.AllowedSourcesComplianceService;
import dev.prospectos.infrastructure.service.compliance.AllowedSourcesProperties;
import dev.prospectos.infrastructure.service.inmemory.InMemoryLeadResultFactory;
import dev.prospectos.infrastructure.service.scoring.CompanyScoringService;
@Service
@Profile("development")
public class ScraperLeadSearchService implements LeadSearchService {
    private final ScraperLeadRequestResolver requestResolver;
    private final ScraperLeadSourceDispatcher sourceDispatcher;
    private final ScraperLeadIcpLoader icpLoader;
    private final ScraperLeadResultRanker resultRanker = new ScraperLeadResultRanker();
    private final ScraperLeadResponseFactory responseFactory = new ScraperLeadResponseFactory();

    public ScraperLeadSearchService(
        ScraperClientInterface scraperClient,
        CompanyEnrichmentService enrichmentService,
        CompanyDataService companyDataService,
        LeadDiscoveryService leadDiscoveryService,
        ICPDataService icpDataService,
        CompanyScoringService scoringService,
        AllowedSourcesComplianceService complianceService,
        AllowedSourcesProperties allowedSourcesProperties,
        LeadSearchProperties properties,
        QueryMetricsRecorder queryMetricsRecorder
    ) {
        this.requestResolver = new ScraperLeadRequestResolver(complianceService, properties);
        this.icpLoader = new ScraperLeadIcpLoader(icpDataService);
        InMemoryLeadResultFactory inMemoryResultFactory = new InMemoryLeadResultFactory(scoringService);
        ScraperLeadCandidateFactory candidateFactory = new ScraperLeadCandidateFactory();
        ScraperWebsiteLeadSearch websiteLeadSearch = new ScraperWebsiteLeadSearch(
            scraperClient,
            enrichmentService,
            scoringService,
            candidateFactory
        );
        this.sourceDispatcher = new ScraperLeadSourceDispatcher(
            companyDataService,
            leadDiscoveryService,
            inMemoryResultFactory,
            websiteLeadSearch,
            ScraperLeadDiscoverySourceConfig.fromAllowedSources(allowedSourcesProperties.allowedSources()),
            queryMetricsRecorder
        );
    }

    @Override
    public LeadSearchResponse searchLeads(LeadSearchRequest request) {
        ScraperLeadRequestContext context = requestResolver.resolve(request);
        Long icpId = requestResolver.resolveIcpId(request.icpId());
        ICP icp = icpLoader.load(icpId);
        List<String> failures = new ArrayList<>();
        List<LeadResultDTO> aggregated = new ArrayList<>();
        for (String source : context.sources()) {
            try {
                aggregated.addAll(sourceDispatcher.searchBySource(source, context, icpId, icp));
            } catch (RuntimeException exception) {
                failures.add(source + ": " + messageOrFallback(exception));
            }
        }

        List<LeadResultDTO> leads = resultRanker.deduplicateSortAndLimit(aggregated, context.limit());
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

    private String messageOrFallback(RuntimeException exception) {
        if (exception == null || exception.getMessage() == null || exception.getMessage().isBlank()) {
            return "Source search failed";
        }
        return exception.getMessage();
    }
}
