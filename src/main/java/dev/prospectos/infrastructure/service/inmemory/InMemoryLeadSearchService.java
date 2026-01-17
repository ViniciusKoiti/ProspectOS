package dev.prospectos.infrastructure.service.inmemory;

import dev.prospectos.api.ICPDataService;
import dev.prospectos.api.LeadSearchService;
import dev.prospectos.api.dto.CompanyCandidateDTO;
import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.api.dto.ICPDto;
import dev.prospectos.api.dto.LeadResultDTO;
import dev.prospectos.api.dto.LeadSearchRequest;
import dev.prospectos.api.dto.LeadSearchResponse;
import dev.prospectos.api.dto.LeadSearchStatus;
import dev.prospectos.api.dto.ScoreDTO;
import dev.prospectos.api.dto.SourceProvenanceDTO;
import dev.prospectos.api.mapper.CompanyMapper;
import dev.prospectos.core.domain.Company;
import dev.prospectos.core.domain.ICP;
import dev.prospectos.core.util.LeadKeyGenerator;
import dev.prospectos.infrastructure.config.LeadSearchProperties;
import dev.prospectos.infrastructure.service.compliance.AllowedSourcesComplianceService;
import dev.prospectos.infrastructure.service.scoring.CompanyScoringService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * In-memory lead search for demo and test profiles.
 */
@Service
@Profile({"demo", "test", "mock"})
public class InMemoryLeadSearchService implements LeadSearchService {

    private static final int DEFAULT_LIMIT = 10;

    private final InMemoryCoreDataStore store;
    private final ICPDataService icpDataService;
    private final CompanyScoringService scoringService;
    private final AllowedSourcesComplianceService complianceService;
    private final LeadSearchProperties properties;

    public InMemoryLeadSearchService(
        InMemoryCoreDataStore store,
        ICPDataService icpDataService,
        CompanyScoringService scoringService,
        AllowedSourcesComplianceService complianceService,
        LeadSearchProperties properties
    ) {
        this.store = store;
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
        String[] tokens = request.query().toLowerCase(Locale.ROOT).split("\\s+");
        List<String> requestedSources = complianceService.validateSources(request.sources());
        String sourceName = resolveSourceName(requestedSources);

        Long icpId = resolveIcpId(request.icpId());
        ICPDto icpDto = icpDataService.findICP(icpId);
        if (icpDto == null) {
            throw new IllegalArgumentException("ICP not found with id: " + icpId);
        }
        ICP icp = toDomainICP(icpDto);

        List<LeadResultDTO> leads = store.companies().values().stream()
            .filter(company -> matchesQuery(company, tokens))
            .limit(limit)
            .map(company -> toLeadResult(company, sourceName, icp))
            .toList();

        return new LeadSearchResponse(
            LeadSearchStatus.COMPLETED,
            leads,
            UUID.randomUUID(),
            "In-memory search completed"
        );
    }

    private boolean matchesQuery(CompanyDTO company, String[] tokens) {
        String haystack = String.join(" ",
            safe(company.name()),
            safe(company.industry()),
            safe(company.location()),
            safe(company.description())
        ).toLowerCase(Locale.ROOT);

        for (String token : tokens) {
            if (!token.isBlank() && haystack.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private LeadResultDTO toLeadResult(CompanyDTO companyDTO, String sourceName, ICP icp) {
        Company company = CompanyMapper.toDomain(companyDTO);
        ScoreDTO score = scoringService.scoreCandidate(company, icp);

        CompanyCandidateDTO candidate = new CompanyCandidateDTO(
            companyDTO.name(),
            companyDTO.website(),
            companyDTO.industry(),
            companyDTO.description(),
            companyDTO.size(),
            companyDTO.location(),
            List.of() // in-memory doesn't have contact data
        );

        String leadKey = LeadKeyGenerator.generate(companyDTO.website(), sourceName);

        SourceProvenanceDTO provenance = new SourceProvenanceDTO(
            sourceName,
            companyDTO.website(),
            Instant.now()
        );

        return new LeadResultDTO(candidate, score, provenance, leadKey);
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

    private String resolveSourceName(List<String> sources) {
        if (sources == null || sources.isEmpty()) {
            return "in-memory";
        }
        return sources.stream()
            .filter(Objects::nonNull)
            .filter(source -> !source.isBlank())
            .findFirst()
            .orElse("in-memory");
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
