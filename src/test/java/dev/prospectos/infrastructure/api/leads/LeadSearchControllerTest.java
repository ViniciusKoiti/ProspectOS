package dev.prospectos.infrastructure.api.leads;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import dev.prospectos.api.LeadSearchService;
import dev.prospectos.api.dto.CompanyCandidateDTO;
import dev.prospectos.api.dto.LeadResultDTO;
import dev.prospectos.api.dto.LeadSearchRequest;
import dev.prospectos.api.dto.LeadSearchResponse;
import dev.prospectos.api.dto.LeadSearchStatus;
import dev.prospectos.api.dto.ScoreDTO;
import dev.prospectos.api.dto.SourceProvenanceDTO;
import dev.prospectos.infrastructure.handler.ApiExceptionHandler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LeadSearchControllerTest {

    @Mock
    private LeadSearchService leadSearchService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new LeadSearchController(leadSearchService))
            .setControllerAdvice(new ApiExceptionHandler())
            .build();
    }

    @Test
    void search_SerializesWebsitePresenceInPayload() throws Exception {
        LeadSearchResponse response = new LeadSearchResponse(
            LeadSearchStatus.COMPLETED,
            List.of(new LeadResultDTO(
                new CompanyCandidateDTO(
                    "Acme",
                    null,
                    "Software",
                    "B2B software supplier",
                    "SMALL",
                    "Sao Paulo",
                    List.of("contato@acme.com"),
                    CompanyCandidateDTO.WebsitePresence.NO_WEBSITE
                ),
                new ScoreDTO(82, "HOT", "Good fit"),
                new SourceProvenanceDTO("in-memory", null, Instant.parse("2026-03-20T00:00:00Z")),
                "lead-key-acme"
            )),
            UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
            "ok"
        );
        when(leadSearchService.searchLeads(any(LeadSearchRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/leads/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "query": "software",
                      "limit": 1,
                      "sources": ["in-memory"],
                      "icpId": 1
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("COMPLETED"))
            .andExpect(jsonPath("$.leads[0].candidate.websitePresence").value("NO_WEBSITE"));
    }
}
