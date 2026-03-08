package dev.prospectos.infrastructure.service.discovery;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import dev.prospectos.api.ICPDataService;
import dev.prospectos.api.LeadDiscoveryService;
import dev.prospectos.api.dto.ICPDto;
import dev.prospectos.api.dto.LeadDiscoveryRequest;
import dev.prospectos.api.dto.LeadResultDTO;
import dev.prospectos.api.dto.LeadSearchResponse;
import dev.prospectos.api.dto.LeadSearchStatus;
import dev.prospectos.core.domain.ICP;
import dev.prospectos.infrastructure.config.LeadSearchProperties;
import dev.prospectos.infrastructure.service.compliance.AllowedSourcesComplianceService;
import dev.prospectos.infrastructure.service.scoring.CompanyScoringService;

/**
 * Default lead discovery orchestrator using source strategies.
 */
@Service
public class DefaultLeadDiscoveryService implements LeadDiscoveryService {

    private static final int DEFAULT_LIMIT = 10;
    private static final String DEFAULT_SOURCE = "llm-discovery";

    private final DiscoverySourceRegistry sourceRegistry;
    private final ICPDataService icpDataService;
    private final AllowedSourcesComplianceService complianceService;
    private final LeadSearchProperties properties;
    private final DiscoveryLeadResultFactory leadResultFactory;

    public DefaultLeadDiscoveryService(
        List<LeadDiscoverySource> sources,
        ICPDataService icpDataService,
        CompanyScoringService scoringService,
        AllowedSourcesComplianceService complianceService,
        LeadSearchProperties properties
    ) {
        this.sourceRegistry = new DiscoverySourceRegistry(sources);
        this.icpDataService = icpDataService;
        this.complianceService = complianceService;
        this.properties = properties;
        this.leadResultFactory = new DiscoveryLeadResultFactory(scoringService, DEFAULT_SOURCE);
    }

    @Override
    public LeadSearchResponse discoverLeads(LeadDiscoveryRequest request) {
        validateRequest(request);
        int limit = request.limit() == null ? DEFAULT_LIMIT : request.limit();
        List<String> validatedSources = validateDiscoverySources(request.sources());
        Long icpId = DiscoveryIcpResolver.resolveIcpId(request.icpId(), properties.defaultIcpId());
        ICPDto icpDto = icpDataService.findICP(icpId);
        if (icpDto == null) {
            throw new IllegalArgumentException("ICP not found with id: " + icpId);
        }
        ICP icp = DiscoveryIcpResolver.toDomain(icpDto);

        DiscoveryContext context = new DiscoveryContext(request.query().trim(), request.role(), limit, icpDto);
        List<DiscoveredLeadCandidate> discovered = sourceRegistry.discover(validatedSources, context);
        List<LeadResultDTO> leads = leadResultFactory.toLeadResults(discovered, icp, limit);

        return new LeadSearchResponse(
            LeadSearchStatus.COMPLETED,
            leads,
            UUID.randomUUID(),
            leads.isEmpty() ? "No leads discovered" : "Lead discovery completed"
        );
    }

    private void validateRequest(LeadDiscoveryRequest request) {
        if (request == null || request.query() == null || request.query().isBlank()) {
            throw new IllegalArgumentException("Query cannot be null or blank");
        }
    }

    private List<String> validateDiscoverySources(List<String> requestedSources) {
        List<String> effectiveSources = (requestedSources == null || requestedSources.isEmpty())
            ? List.of(DEFAULT_SOURCE)
            : requestedSources;
        List<String> validatedSources = complianceService.validateSources(effectiveSources);
        return validatedSources.isEmpty() ? List.of(DEFAULT_SOURCE) : validatedSources;
    }
}
