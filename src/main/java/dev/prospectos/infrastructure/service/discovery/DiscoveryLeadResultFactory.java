package dev.prospectos.infrastructure.service.discovery;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import dev.prospectos.api.dto.CompanyCandidateDTO;
import dev.prospectos.api.dto.LeadResultDTO;
import dev.prospectos.api.dto.ScoreDTO;
import dev.prospectos.api.dto.SourceProvenanceDTO;
import dev.prospectos.core.domain.Company;
import dev.prospectos.core.domain.ICP;
import dev.prospectos.core.domain.Website;
import dev.prospectos.core.util.LeadKeyGenerator;
import dev.prospectos.infrastructure.service.scoring.CompanyScoringService;

final class DiscoveryLeadResultFactory {

    private final CompanyScoringService scoringService;
    private final String defaultSource;

    DiscoveryLeadResultFactory(CompanyScoringService scoringService, String defaultSource) {
        this.scoringService = scoringService;
        this.defaultSource = defaultSource;
    }

    List<LeadResultDTO> toLeadResults(List<DiscoveredLeadCandidate> discovered, ICP icp, int limit) {
        Map<String, LeadResultDTO> deduplicated = new LinkedHashMap<>();
        for (DiscoveredLeadCandidate candidate : discovered) {
            if (deduplicated.size() >= limit) {
                break;
            }
            Company company = toDomainCompany(candidate);
            if (company == null) {
                continue;
            }
            String sourceName = candidate.sourceName() == null ? defaultSource : candidate.sourceName();
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
}
