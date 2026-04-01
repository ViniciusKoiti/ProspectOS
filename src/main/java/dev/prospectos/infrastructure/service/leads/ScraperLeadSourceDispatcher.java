package dev.prospectos.infrastructure.service.leads;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import dev.prospectos.api.CompanyDataService;
import dev.prospectos.api.LeadDiscoveryService;
import dev.prospectos.api.dto.LeadDiscoveryRequest;
import dev.prospectos.api.dto.LeadResultDTO;
import dev.prospectos.api.dto.LeadSearchResponse;
import dev.prospectos.api.dto.LeadSearchStatus;
import dev.prospectos.api.mcp.QueryMetricsRecorder;
import dev.prospectos.core.domain.ICP;
import dev.prospectos.infrastructure.mcp.service.QueryMetricsExecutionTracker;
import dev.prospectos.infrastructure.service.inmemory.InMemoryLeadQueryMatcher;
import dev.prospectos.infrastructure.service.inmemory.InMemoryLeadResultFactory;

final class ScraperLeadSourceDispatcher {

    private final CompanyDataService companyDataService;
    private final LeadDiscoveryService leadDiscoveryService;
    private final InMemoryLeadResultFactory inMemoryLeadResultFactory;
    private final ScraperWebsiteLeadSearch websiteLeadSearch;
    private final Set<String> discoverySources;
    private final QueryMetricsRecorder metricsRecorder;

    ScraperLeadSourceDispatcher(
        CompanyDataService companyDataService,
        LeadDiscoveryService leadDiscoveryService,
        InMemoryLeadResultFactory inMemoryLeadResultFactory,
        ScraperWebsiteLeadSearch websiteLeadSearch,
        Set<String> discoverySources,
        QueryMetricsRecorder metricsRecorder
    ) {
        this.companyDataService = companyDataService;
        this.leadDiscoveryService = leadDiscoveryService;
        this.inMemoryLeadResultFactory = inMemoryLeadResultFactory;
        this.websiteLeadSearch = websiteLeadSearch;
        this.discoverySources = discoverySources == null ? Set.of() : discoverySources;
        this.metricsRecorder = metricsRecorder;
    }

    List<LeadResultDTO> searchBySource(String source, ScraperLeadRequestContext context, Long icpId, ICP icp) {
        String normalizedSource = normalize(source);
        if ("in-memory".equals(normalizedSource)) {
            return QueryMetricsExecutionTracker.track(metricsRecorder, normalizedSource, context.query(), () -> searchInMemory(normalizedSource, context, icp));
        }
        if (discoverySources.contains(normalizedSource)) {
            return searchDiscovery(normalizedSource, context, icpId);
        }
        return QueryMetricsExecutionTracker.track(metricsRecorder, normalizedSource, context.query(), () -> websiteLeadSearch.search(normalizedSource, context, icp));
    }

    private List<LeadResultDTO> searchInMemory(String source, ScraperLeadRequestContext context, ICP icp) {
        var tokens = InMemoryLeadQueryMatcher.tokens(context.query());
        return companyDataService.findAllCompanies().stream()
            .filter(company -> InMemoryLeadQueryMatcher.matches(company, tokens))
            .limit(context.limit())
            .map(company -> inMemoryLeadResultFactory.toLeadResult(company, source, icp))
            .toList();
    }

    private List<LeadResultDTO> searchDiscovery(String source, ScraperLeadRequestContext context, Long icpId) {
        LeadSearchResponse response = leadDiscoveryService.discoverLeads(new LeadDiscoveryRequest(
            context.query(),
            null,
            context.limit(),
            List.of(source),
            icpId
        ));
        if (response.status() != LeadSearchStatus.COMPLETED || response.leads() == null) {
            throw new IllegalStateException(response.message() == null ? "Lead discovery source failed" : response.message());
        }
        return response.leads();
    }

    private String normalize(String source) {
        if (source == null || source.isBlank()) {
            throw new IllegalArgumentException("Lead source cannot be blank");
        }
        return source.trim().toLowerCase(Locale.ROOT);
    }
}
