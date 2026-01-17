package dev.prospectos.infrastructure.service.leads;

import dev.prospectos.api.CompanyDataService;
import dev.prospectos.api.SourceProvenanceService;
import dev.prospectos.api.dto.CompanyCandidateDTO;
import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.api.dto.ScoreDTO;
import dev.prospectos.api.dto.request.AcceptLeadRequest;
import dev.prospectos.api.dto.request.CompanyCreateRequest;
import dev.prospectos.api.dto.response.AcceptLeadResponse;
import dev.prospectos.core.util.LeadKeyGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for accepting leads from preview and persisting them.
 * Handles validation, deduplication, and persistence of company data.
 */
@Service
@Slf4j
public class LeadAcceptService {

    private static final int MIN_SCORE = 0;
    private static final int MAX_SCORE = 100;
    private static final String DEFAULT_PRIORITY = "COLD";

    private final CompanyDataService companyDataService;
    private final SourceProvenanceService sourceProvenanceService;

    public LeadAcceptService(
        CompanyDataService companyDataService,
        SourceProvenanceService sourceProvenanceService
    ) {
        this.companyDataService = companyDataService;
        this.sourceProvenanceService = sourceProvenanceService;
    }

    /**
     * Accepts a lead from preview and persists it.
     * Validates leadKey, sanitizes score, and handles deduplication.
     *
     * @param request the accept lead request with full lead data
     * @return response with persisted company
     * @throws IllegalArgumentException if leadKey is invalid or data is malformed
     */
    public AcceptLeadResponse acceptLead(AcceptLeadRequest request) {
        validateLeadKey(request.leadKey());

        CompanyCandidateDTO candidate = request.candidate();
        ScoreDTO sanitizedScore = sanitizeScore(request.score());

        // Check for existing company by website domain
        CompanyDTO existingCompany = companyDataService.findCompanyByWebsite(candidate.website());

        CompanyDTO company;
        String message;

        if (existingCompany != null) {
            log.info("Lead with website {} already exists as company {}, updating score",
                candidate.website(), existingCompany.id());
            companyDataService.updateCompanyScore(existingCompany.id(), sanitizedScore);
            company = companyDataService.findCompany(existingCompany.id());
            message = "Lead accepted and updated (already existed)";
        } else {
            CompanyCreateRequest createRequest = new CompanyCreateRequest(
                candidate.name(),
                candidate.industry() != null ? candidate.industry() : "Other",
                candidate.website(),
                candidate.description(),
                null, // location - not in CompanyCreateRequest yet
                sanitizedScore,
                candidate.size()
            );

            company = companyDataService.createCompany(createRequest);
            log.info("Created new company {} from lead with key {}", company.id(), request.leadKey());
            message = "Lead accepted and created";
        }

        // Record source provenance
        if (request.source() != null) {
            sourceProvenanceService.record(company, request.source());
        }

        return new AcceptLeadResponse(company, message);
    }

    private void validateLeadKey(String leadKey) {
        if (!LeadKeyGenerator.isValid(leadKey)) {
            throw new IllegalArgumentException("Invalid leadKey format");
        }
    }

    private ScoreDTO sanitizeScore(ScoreDTO score) {
        if (score == null) {
            return new ScoreDTO(MIN_SCORE, DEFAULT_PRIORITY, "No score provided");
        }

        int boundedScore = clamp(score.value());
        String priority = normalizePriority(score.category());
        String reasoning = score.reasoning() != null && !score.reasoning().isBlank()
            ? score.reasoning()
            : "Accepted from lead preview";

        return new ScoreDTO(boundedScore, priority, reasoning);
    }

    private int clamp(int score) {
        if (score < MIN_SCORE) {
            return MIN_SCORE;
        }
        if (score > MAX_SCORE) {
            return MAX_SCORE;
        }
        return score;
    }

    private String normalizePriority(String category) {
        if (category == null || category.isBlank()) {
            return DEFAULT_PRIORITY;
        }

        String normalized = category.trim().toUpperCase();

        // Validate against known priorities
        return switch (normalized) {
            case "HOT", "WARM", "COLD", "IGNORE" -> normalized;
            default -> {
                log.warn("Unknown priority category '{}', defaulting to {}", category, DEFAULT_PRIORITY);
                yield DEFAULT_PRIORITY;
            }
        };
    }
}
