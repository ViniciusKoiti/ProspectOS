package dev.prospectos.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.prospectos.ai.client.ScraperClientInterface;
import dev.prospectos.api.CompanyDataService;
import dev.prospectos.api.LeadSearchService;
import dev.prospectos.api.SourceProvenanceService;
import dev.prospectos.api.dto.LeadSearchRequest;
import dev.prospectos.core.enrichment.CompanyEnrichmentService;
import dev.prospectos.infrastructure.service.compliance.AllowedSourcesComplianceService;
import dev.prospectos.infrastructure.service.leads.ScraperLeadSearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.profiles.active=test")
@AutoConfigureMockMvc
@TestPropertySource(properties = "prospectos.leads.allowed-sources=in-memory,scraper")
@ActiveProfiles("test")
class LeadSearchScraperIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void leadSearch_UsesScraperAndPersistsCompany() throws Exception {
        LeadSearchRequest request = new LeadSearchRequest(
            "https://acme.com",
            1,
            List.of("scraper"),
            null
        );

        mockMvc.perform(post("/api/leads/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("COMPLETED"))
            .andExpect(jsonPath("$.leads").isArray())
            .andExpect(jsonPath("$.leads.length()").value(1))
            .andExpect(jsonPath("$.leads[0].company.name").value("Example Company"))
            .andExpect(jsonPath("$.leads[0].company.website").value("https://acme.com"))
            .andExpect(jsonPath("$.leads[0].source.sourceName").value("scraper"));
    }

    @TestConfiguration
    static class ScraperLeadSearchTestConfig {

        @Bean
        @Primary
        LeadSearchService scraperLeadSearchService(
            ScraperClientInterface scraperClient,
            CompanyEnrichmentService enrichmentService,
            CompanyDataService companyDataService,
            SourceProvenanceService sourceProvenanceService,
            AllowedSourcesComplianceService complianceService
        ) {
            return new ScraperLeadSearchService(
                scraperClient,
                enrichmentService,
                companyDataService,
                sourceProvenanceService,
                complianceService
            );
        }
    }
}
