package dev.prospectos.infrastructure.service.discovery;

import dev.prospectos.api.ICPDataService;
import dev.prospectos.api.LeadDiscoveryService;
import dev.prospectos.api.dto.CompanyCandidateDTO;
import dev.prospectos.api.dto.ICPDto;
import dev.prospectos.api.dto.LeadDiscoveryRequest;
import dev.prospectos.api.dto.LeadResultDTO;
import dev.prospectos.api.dto.LeadSearchResponse;
import dev.prospectos.api.dto.LeadSearchStatus;
import dev.prospectos.api.dto.ScoreDTO;
import dev.prospectos.api.dto.SourceProvenanceDTO;
import dev.prospectos.core.domain.Company;
import dev.prospectos.core.domain.ICP;
import dev.prospectos.core.domain.Website;
import dev.prospectos.core.util.LeadKeyGenerator;
import dev.prospectos.infrastructure.config.LeadSearchProperties;
import dev.prospectos.infrastructure.service.compliance.AllowedSourcesComplianceService;
import dev.prospectos.infrastructure.service.scoring.CompanyScoringService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Default lead discovery orchestrator using source strategies.
 */
@Service
public class DefaultLeadDiscoveryService implements LeadDiscoveryService {

    private static final int DEFAULT_LIMIT = 10;
    private static final String DEFAULT_SOURCE = "llm-discovery";

    private final Map<String, LeadDiscoverySource> sourceRegistry;
    private final ICPDataService icpDataService;
    private final CompanyScoringService scoringService;
    private final AllowedSourcesComplianceService complianceService;
    private final LeadSearchProperties properties;

    public DefaultLeadDiscoveryService(
        List<LeadDiscoverySource> sources,
        ICPDataService icpDataService,
        CompanyScoringService scoringService,
        AllowedSourcesComplianceService complianceService,
        LeadSearchProperties properties
    ) {
        this.sourceRegistry = indexSources(sources);
        this.icpDataService = icpDataService;
        this.scoringService = scoringService;
        this.complianceService = complianceService;
        this.properties = properties;
    }

    @Override
    public LeadSearchResponse discoverLeads(LeadDiscoveryRequest request) {
        if (request == null || request.query() == null || request.query().isBlank()) {
            throw new IllegalArgumentException("Query cannot be null or blank");
        }

        int limit = request.limit() == null ? DEFAULT_LIMIT : request.limit();
        List<String> validatedSources = validateDiscoverySources(request.sources());

        Long icpId = resolveIcpId(request.icpId());
        ICPDto icpDto = icpDataService.findICP(icpId);
        if (icpDto == null) {
            throw new IllegalArgumentException("ICP not found with id: " + icpId);
        }
        ICP icp = toDomainICP(icpDto);

        DiscoveryContext context = new DiscoveryContext(request.query().trim(), request.role(), limit, icpDto);
        List<DiscoveredLeadCandidate> discovered = new ArrayList<>();
        for (String sourceName : validatedSources) {
            LeadDiscoverySource source = sourceRegistry.get(sourceName);
            if (source == null) {
                continue;
            }
            discovered.addAll(source.discover(context));
        }

        List<LeadResultDTO> leads = toLeadResults(discovered, icp, limit);
        return new LeadSearchResponse(
            LeadSearchStatus.COMPLETED,
            leads,
            UUID.randomUUID(),
            leads.isEmpty() ? "No leads discovered" : "Lead discovery completed"
        );
    }

    private List<String> validateDiscoverySources(List<String> requestedSources) {
        List<String> effective = (requestedSources == null || requestedSources.isEmpty())
            ? List.of(DEFAULT_SOURCE)
            : requestedSources;
        List<String> validated = complianceService.validateSources(effective);
        if (validated.isEmpty()) {
            return List.of(DEFAULT_SOURCE);
        }
        return validated;
    }

    private List<LeadResultDTO> toLeadResults(List<DiscoveredLeadCandidate> discovered, ICP icp, int limit) {
        Map<String, LeadResultDTO> deduplicated = new LinkedHashMap<>();

        for (DiscoveredLeadCandidate candidate : discovered) {
            if (deduplicated.size() >= limit) {
                break;
            }

            Company company = toDomainCompany(candidate);
            if (company == null) {
                continue;
            }

            String sourceName = candidate.sourceName() == null ? DEFAULT_SOURCE : candidate.sourceName();
            String leadKey = LeadKeyGenerator.generate(candidate.website(), sourceName);
            if (deduplicated.containsKey(leadKey)) {
                continue;
            }

            ScoreDTO score = scoringService.scoreCandidate(company, icp);
            CompanyCandidateDTO dto = new CompanyCandidateDTO(
                company.getName(),
                company.getWebsite().getUrl(),
                company.getIndustry(),
                company.getDescription(),
                company.getSize() != null ? company.getSize().name() : null,
                candidate.location(),
                candidate.contacts() == null ? List.of() : candidate.contacts()
            );
            SourceProvenanceDTO provenance = new SourceProvenanceDTO(sourceName, candidate.website(), Instant.now());
            deduplicated.put(leadKey, new LeadResultDTO(dto, score, provenance, leadKey));
        }

        return List.copyOf(deduplicated.values());
    }

    private Company toDomainCompany(DiscoveredLeadCandidate candidate) {
        if (candidate == null || candidate.name() == null || candidate.website() == null) {
            return null;
        }

        try {
            Website website = Website.of(candidate.website());
            Company company = Company.create(candidate.name(), website, normalizeIndustry(candidate.industry()));
            if (candidate.description() != null && !candidate.description().isBlank()) {
                company.setDescription(candidate.description());
            }
            return company;
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private String normalizeIndustry(String industry) {
        if (industry == null || industry.isBlank()) {
            return "Other";
        }
        return industry.trim();
    }

    private Long resolveIcpId(Long requestIcpId) {
        if (requestIcpId != null) {
            return requestIcpId;
        }
        if (properties.defaultIcpId() == null) {
            throw new IllegalArgumentException(
                "ICP ID is required. Provide icpId in request or configure prospectos.leads.default-icp-id"
            );
        }
        return properties.defaultIcpId();
    }

    private ICP toDomainICP(ICPDto icpDto) {
        return ICP.create(
            icpDto.name(),
            icpDto.description(),
            icpDto.targetIndustries() == null ? List.of() : icpDto.targetIndustries(),
            icpDto.regions() == null ? List.of() : icpDto.regions(),
            icpDto.targetRoles() == null ? List.of() : icpDto.targetRoles(),
            icpDto.interestTheme()
        );
    }

    private Map<String, LeadDiscoverySource> indexSources(List<LeadDiscoverySource> sources) {
        Map<String, LeadDiscoverySource> map = new LinkedHashMap<>();
        for (LeadDiscoverySource source : sources) {
            map.put(source.sourceName(), source);
        }
        return map;
    }
}
