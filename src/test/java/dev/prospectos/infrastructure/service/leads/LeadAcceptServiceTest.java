package dev.prospectos.infrastructure.service.leads;

import dev.prospectos.api.CompanyDataService;
import dev.prospectos.api.SourceProvenanceService;
import dev.prospectos.api.dto.CompanyCandidateDTO;
import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.api.dto.ScoreDTO;
import dev.prospectos.api.dto.SourceProvenanceDTO;
import dev.prospectos.api.dto.request.AcceptLeadRequest;
import dev.prospectos.api.dto.request.CompanyCreateRequest;
import dev.prospectos.api.dto.response.AcceptLeadResponse;
import dev.prospectos.core.util.LeadKeyGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeadAcceptServiceTest {

    @Mock
    private CompanyDataService companyDataService;

    @Mock
    private SourceProvenanceService sourceProvenanceService;

    private LeadAcceptService service;

    @BeforeEach
    void setUp() {
        service = new LeadAcceptService(companyDataService, sourceProvenanceService);
    }

    @Test
    void acceptLead_DeduplicatesByNormalizedDomain() {
        CompanyDTO existing = new CompanyDTO(
            10L,
            "Acme",
            "Software",
            "https://acme.com",
            "Existing",
            null,
            "Sao Paulo",
            null
        );
        CompanyDTO updated = new CompanyDTO(
            10L,
            "Acme",
            "Software",
            "https://acme.com",
            "Existing",
            null,
            "Sao Paulo",
            new ScoreDTO(80, "HOT", "match")
        );

        when(companyDataService.findByWebsite("http://www.acme.com")).thenReturn(existing);
        when(companyDataService.findCompany(10L)).thenReturn(updated);

        AcceptLeadRequest request = buildRequest("http://www.acme.com", "llm-discovery");

        AcceptLeadResponse response = service.acceptLead(request);

        assertEquals("Lead accepted and updated (already existed)", response.message());
        assertEquals(10L, response.company().id());
        verify(companyDataService).findByWebsite("http://www.acme.com");
        verify(companyDataService).updateCompanyScore(eq(10L), any(ScoreDTO.class));
        verify(companyDataService, never()).createCompany(any(CompanyCreateRequest.class));
    }

    @Test
    void acceptLead_DoesNotScanAllCompaniesForWebsiteDeduplication() {
        CompanyDTO existing = new CompanyDTO(
            11L,
            "Beta",
            "Software",
            "https://beta.com",
            "Existing",
            null,
            "Curitiba",
            null
        );
        CompanyDTO updated = new CompanyDTO(
            11L,
            "Beta",
            "Software",
            "https://beta.com",
            "Existing",
            null,
            "Curitiba",
            new ScoreDTO(80, "HOT", "match")
        );

        when(companyDataService.findByWebsite("https://beta.com")).thenReturn(existing);
        when(companyDataService.findCompany(11L)).thenReturn(updated);

        AcceptLeadRequest request = buildRequest("https://beta.com", "apollo");
        service.acceptLead(request);

        verify(companyDataService).findByWebsite("https://beta.com");
        verify(companyDataService, never()).findAllCompanies();
    }

    private AcceptLeadRequest buildRequest(String website, String sourceName) {
        CompanyCandidateDTO candidate = new CompanyCandidateDTO(
            "Candidate",
            website,
            "Software",
            "Desc",
            "SMALL",
            "Brazil",
            List.of("contact@example.com")
        );
        ScoreDTO score = new ScoreDTO(80, "HOT", "match");
        SourceProvenanceDTO source = new SourceProvenanceDTO(sourceName, website, Instant.now());
        String leadKey = LeadKeyGenerator.generate(website, sourceName);
        return new AcceptLeadRequest(leadKey, candidate, score, source);
    }
}
