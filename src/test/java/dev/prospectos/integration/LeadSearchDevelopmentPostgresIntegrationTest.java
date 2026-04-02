package dev.prospectos.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.prospectos.ai.client.ScraperClientInterface;
import dev.prospectos.api.CompanyDataService;
import dev.prospectos.api.ICPDataService;
import dev.prospectos.api.dto.LeadSearchRequest;
import dev.prospectos.api.dto.request.CompanyCreateRequest;
import dev.prospectos.api.mcp.QueryMetricsService;
import dev.prospectos.api.mcp.QueryTimeWindow;
import dev.prospectos.support.PostgresIntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"development", "test-pg"})
@TestPropertySource(properties = {
    "prospectos.ai.active-providers=groq",
    "prospectos.ai.groq.api-key=local-groq-key-123",
    "scraper.ai.enabled=false",
    "prospectos.leads.allowed-sources=in-memory,scraper",
    "prospectos.leads.default-sources=in-memory"
})
class LeadSearchDevelopmentPostgresIntegrationTest extends PostgresIntegrationTestBase {

    @MockitoBean
    private ScraperClientInterface scraperClient;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CompanyDataService companyDataService;

    @Autowired
    private ICPDataService icpDataService;

    @Autowired
    private QueryMetricsService queryMetricsService;

    @Test
    void leadSearchInDevelopment_usesPostgresDataAndRecordsMetrics() throws Exception {
        companyDataService.createCompany(new CompanyCreateRequest(
            "Development Search Co",
            "Software",
            "https://development-search.example.com",
            "Development profile search validation",
            "BR",
            "Campinas",
            "MEDIUM"
        ));

        mockMvc.perform(post("/api/leads/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LeadSearchRequest("software", 3, List.of("in-memory"), existingIcpId()))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("COMPLETED"))
            .andExpect(jsonPath("$.leads[0].candidate.name").value("Development Search Co"));

        var metrics = queryMetricsService.getMetrics(QueryTimeWindow.ONE_HOUR, "in-memory");
        assertThat(metrics.totalQueries()).isGreaterThanOrEqualTo(1);
        assertThat(metrics.providerBreakdown()).isNotEmpty();
    }

    private Long existingIcpId() {
        return icpDataService.findAllICPs().stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No ICP seeded for integration test"))
            .id();
    }
}
