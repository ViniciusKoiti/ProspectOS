package dev.prospectos.infrastructure.service.discovery;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Discovery source backed by in-memory semantic vector search over known companies.
 */
@Component
@ConditionalOnProperty(prefix = "prospectos.discovery.vector", name = "enabled", havingValue = "true")
public class VectorCompanyLeadDiscoverySource implements LeadDiscoverySource {

    static final String SOURCE_NAME = "vector-company";

    private final SemanticCompanySearchService semanticSearchService;

    public VectorCompanyLeadDiscoverySource(SemanticCompanySearchService semanticSearchService) {
        this.semanticSearchService = semanticSearchService;
    }

    @Override
    public String sourceName() {
        return SOURCE_NAME;
    }

    @Override
    public List<DiscoveredLeadCandidate> discover(DiscoveryContext context) {
        return semanticSearchService.search(context.query(), context.limit()).stream()
            .map(match -> new DiscoveredLeadCandidate(
                match.company().name(),
                match.company().website(),
                match.company().industry(),
                match.company().description(),
                match.company().location(),
                List.of(),
                SOURCE_NAME
            ))
            .toList();
    }
}
