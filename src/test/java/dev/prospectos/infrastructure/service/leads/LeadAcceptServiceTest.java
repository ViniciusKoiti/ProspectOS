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

    @Mock
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

    @Test
    void acceptLead_UsesDefaultScoreWhenRequestScoreIsNull() {
        CompanyDTO existing = new CompanyDTO(
            12L,
            "Gamma",
            "Software",
            "https://gamma.com",
            "Existing",
            null,
            "Florianopolis",
            null
        );
        CompanyDTO updated = new CompanyDTO(
            12L,
            "Gamma",
            "Software",
            "https://gamma.com",
            "Existing",
            null,
            "Florianopolis",
            new ScoreDTO(0, "COLD", "No score provided")
        );

        when(companyDataService.findByWebsite("https://gamma.com")).thenReturn(existing);
        when(companyDataService.findCompany(12L)).thenReturn(updated);

        CompanyCandidateDTO candidate = new CompanyCandidateDTO(
            "Gamma",
            "https://gamma.com",
            "Software",
            "Desc",
            "SMALL",
            "Brazil",
            List.of("contact@gamma.com")
        );
        SourceProvenanceDTO source = new SourceProvenanceDTO("apollo", "https://gamma.com", Instant.now());
        String leadKey = LeadKeyGenerator.generate("https://gamma.com", "apollo");
        AcceptLeadRequest request = new AcceptLeadRequest(leadKey, candidate, null, source);

        service.acceptLead(request);

        ArgumentCaptor<ScoreDTO> captor = ArgumentCaptor.forClass(ScoreDTO.class);
        verify(companyDataService).updateCompanyScore(eq(12L), captor.capture());
        assertEquals(0, captor.getValue().value());
        assertEquals("COLD", captor.getValue().category());
        assertEquals("No score provided", captor.getValue().reasoning());
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
