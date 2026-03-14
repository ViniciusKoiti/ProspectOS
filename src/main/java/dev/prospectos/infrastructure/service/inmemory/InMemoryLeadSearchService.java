package dev.prospectos.infrastructure.service.inmemory;

import java.util.List;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import dev.prospectos.api.CompanyDataService;
import dev.prospectos.api.ICPDataService;
import dev.prospectos.api.LeadSearchService;
import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.api.dto.ICPDto;
import dev.prospectos.api.dto.LeadResultDTO;
import dev.prospectos.api.dto.LeadSearchRequest;
import dev.prospectos.api.dto.LeadSearchResponse;
import dev.prospectos.api.dto.LeadSearchStatus;
import dev.prospectos.core.domain.ICP;
import dev.prospectos.infrastructure.config.LeadSearchProperties;
import dev.prospectos.infrastructure.service.compliance.AllowedSourcesComplianceService;
import dev.prospectos.infrastructure.service.scoring.CompanyScoringService;

/**
 * In-memory lead search for demo and test profiles.
 */
@Service
@Profile("test")
public class InMemoryLeadSearchService implements LeadSearchService {

    private static final int DEFAULT_LIMIT = 10;

    private final CompanyDataService companyDataService;
    private final ICPDataService icpDataService;
    private final AllowedSourcesComplianceService complianceService;
    private final LeadSearchProperties properties;
    private final InMemoryLeadResultFactory leadResultFactory;

    public InMemoryLeadSearchService(
        CompanyDataService companyDataService,
        ICPDataService icpDataService,
        CompanyScoringService scoringService,
        AllowedSourcesComplianceService complianceService,
        LeadSearchProperties properties
    ) {
        this.companyDataService = companyDataService;
        this.icpDataService = icpDataService;
        this.complianceService = complianceService;
        this.properties = properties;
        this.leadResultFactory = new InMemoryLeadResultFactory(scoringService);
    }

    @Override
    public LeadSearchResponse searchLeads(LeadSearchRequest request) {
        validateRequest(request);
        int limit = request.limit() == null ? DEFAULT_LIMIT : request.limit();
        List<String> tokens = InMemoryLeadQueryMatcher.tokens(request.query());
        List<String> requestedSources = complianceService.validateSources(request.sources());
        String sourceName = InMemoryLeadSourceResolver.resolve(requestedSources);
        ICP icp = resolveIcp(request.icpId());

        List<CompanyDTO> companies = companyDataService.findAllCompanies();
        List<LeadResultDTO> leads = companies.stream()
            .filter(company -> InMemoryLeadQueryMatcher.matches(company, tokens))
            .limit(limit)
            .map(company -> leadResultFactory.toLeadResult(company, sourceName, icp))
            .toList();

        return new LeadSearchResponse(
            LeadSearchStatus.COMPLETED,
            leads,
            UUID.randomUUID(),
            "In-memory search completed"
        );
    }

    private void validateRequest(LeadSearchRequest request) {
        if (request == null || request.query() == null || request.query().isBlank()) {
            throw new IllegalArgumentException("Query cannot be null or blank");
        }
    }

    private ICP resolveIcp(Long requestIcpId) {
        Long icpId = InMemoryLeadIcpResolver.resolveIcpId(requestIcpId, properties.defaultIcpId());
        ICPDto icpDto = icpDataService.findICP(icpId);
        if (icpDto == null) {
            throw new IllegalArgumentException("ICP not found with id: " + icpId);
        }
        return InMemoryLeadIcpResolver.toDomain(icpDto);
    }
}
