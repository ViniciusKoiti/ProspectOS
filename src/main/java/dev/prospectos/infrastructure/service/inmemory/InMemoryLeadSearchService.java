package dev.prospectos.infrastructure.service.inmemory;

import dev.prospectos.api.LeadSearchService;
import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.api.dto.LeadResultDTO;
import dev.prospectos.api.dto.LeadSearchRequest;
import dev.prospectos.api.dto.LeadSearchResponse;
import dev.prospectos.api.dto.LeadSearchStatus;
import dev.prospectos.api.dto.ScoreDTO;
import dev.prospectos.api.dto.SourceProvenanceDTO;
import dev.prospectos.infrastructure.service.compliance.AllowedSourcesComplianceService;
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
    private final AllowedSourcesComplianceService complianceService;

    public InMemoryLeadSearchService(
        InMemoryCoreDataStore store,
        AllowedSourcesComplianceService complianceService
    ) {
        this.store = store;
        this.complianceService = complianceService;
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

        List<LeadResultDTO> leads = store.companies().values().stream()
            .filter(company -> matchesQuery(company, tokens))
            .limit(limit)
            .map(company -> toLeadResult(company, sourceName))
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

    private LeadResultDTO toLeadResult(CompanyDTO company, String sourceName) {
        ScoreDTO score = store.companyScores().get(company.id());
        SourceProvenanceDTO provenance = new SourceProvenanceDTO(
            sourceName,
            company.website(),
            Instant.now()
        );
        return new LeadResultDTO(company, score, provenance);
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
