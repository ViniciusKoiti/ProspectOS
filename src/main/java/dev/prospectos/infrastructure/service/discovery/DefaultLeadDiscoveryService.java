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
import dev.prospectos.api.mcp.QueryMetricsRecorder;
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
    private static final String NO_SOURCE_CONFIGURED_MESSAGE =
        "No lead sources configured. Configure prospectos.leads.default-sources or provide sources in request";

    private final DiscoverySourceRegistry sourceRegistry;
    private final ICPDataService icpDataService;
    private final CompanyScoringService scoringService;
    private final AllowedSourcesComplianceService complianceService;
    private final LeadSearchProperties properties;

    public DefaultLeadDiscoveryService(
        List<LeadDiscoverySource> sources,
        ICPDataService icpDataService,
        CompanyScoringService scoringService,
        AllowedSourcesComplianceService complianceService,
        LeadSearchProperties properties,
        QueryMetricsRecorder queryMetricsRecorder
    ) {
        this.sourceRegistry = new DiscoverySourceRegistry(sources, queryMetricsRecorder);
        this.icpDataService = icpDataService;
        this.scoringService = scoringService;
        this.complianceService = complianceService;
        this.properties = properties;
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
        DiscoveryLeadResultFactory leadResultFactory = new DiscoveryLeadResultFactory(scoringService, validatedSources.getFirst());
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
        List<String> validatedSources = complianceService.validateSources(requestedSources);
        if (validatedSources == null || validatedSources.isEmpty()) {
            throw new IllegalArgumentException(NO_SOURCE_CONFIGURED_MESSAGE);
        }
        return validatedSources;
    }
}
