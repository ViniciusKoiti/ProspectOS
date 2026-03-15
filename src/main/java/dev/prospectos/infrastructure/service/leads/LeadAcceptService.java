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

@Service
@Slf4j
public class LeadAcceptService {

    private final CompanyDataService companyDataService;
    private final SourceProvenanceService sourceProvenanceService;
    private final LeadScoreSanitizer scoreSanitizer;

    public LeadAcceptService(
        CompanyDataService companyDataService,
        SourceProvenanceService sourceProvenanceService
    ) {
        this.companyDataService = companyDataService;
        this.sourceProvenanceService = sourceProvenanceService;
        this.scoreSanitizer = new LeadScoreSanitizer();
    }

    public AcceptLeadResponse acceptLead(AcceptLeadRequest request) {
        validateLeadKey(request.leadKey());
        CompanyCandidateDTO candidate = request.candidate();
        ScoreDTO sanitizedScore = scoreSanitizer.sanitize(request.score());
        CompanyDTO existingCompany = companyDataService.findByWebsite(candidate.website());
        CompanyDTO company;
        String message;
        if (existingCompany != null) {
            log.info("Lead with website {} already exists as company {}, updating score",
                candidate.website(), existingCompany.id());
            companyDataService.updateCompanyScore(existingCompany.id(), sanitizedScore);
            persistCandidateContacts(existingCompany.id(), candidate);
            company = companyDataService.findCompany(existingCompany.id());
            message = "Lead accepted and updated (already existed)";
        } else {
            company = companyDataService.createCompany(toCreateRequest(candidate));
            companyDataService.updateCompanyScore(company.id(), sanitizedScore);
            persistCandidateContacts(company.id(), candidate);
            company = companyDataService.findCompany(company.id());
            log.info("Created new company {} from lead with key {}", company.id(), request.leadKey());
            message = "Lead accepted and created";
        }
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

    private void persistCandidateContacts(Long companyId, CompanyCandidateDTO candidate) {
        companyDataService.addCompanyContactEmails(companyId, candidate.contacts());
    }

    private CompanyCreateRequest toCreateRequest(CompanyCandidateDTO candidate) {
        return new CompanyCreateRequest(
            candidate.name(),
            candidate.industry() != null ? candidate.industry() : "Other",
            candidate.website(),
            candidate.description(),
            null,
            null,
            candidate.size()
        );
    }
}
