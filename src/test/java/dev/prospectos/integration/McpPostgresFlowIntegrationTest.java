package dev.prospectos.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.prospectos.api.CompanyDataService;
import dev.prospectos.api.ICPDataService;
import dev.prospectos.api.dto.LeadSearchRequest;
import dev.prospectos.api.dto.request.CompanyCreateRequest;
import dev.prospectos.api.mcp.ProviderRoutingService;
import dev.prospectos.api.mcp.QueryHistoryService;
import dev.prospectos.api.mcp.QueryTimeWindow;
import dev.prospectos.infrastructure.mcp.resources.QueryHistoryMcpResources;
import dev.prospectos.support.PostgresIntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "test-pg"})
@TestPropertySource(properties = "spring.ai.mcp.server.enabled=true")
class McpPostgresFlowIntegrationTest extends PostgresIntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CompanyDataService companyDataService;

    @Autowired
    private ICPDataService icpDataService;

    @Autowired
    private QueryHistoryService queryHistoryService;

    @Autowired
    private ProviderRoutingService providerRoutingService;

    @Autowired
    private QueryHistoryMcpResources queryHistoryMcpResources;

    @Test
    void mcpFlow_usesPostgresBackedDataAndObservedMetrics() throws Exception {
        companyDataService.createCompany(new CompanyCreateRequest(
            "MCP Flow Software",
            "Software",
            "https://mcp-flow.example.com",
            "Prospecting analytics software for GTM teams",
            "BR",
            "Sao Paulo",
            "MEDIUM"
        ));

        mockMvc.perform(post("/api/leads/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LeadSearchRequest("software", 3, List.of("in-memory"), existingIcpId()))))
            .andExpect(status().isOk());

        var history = queryHistoryService.getQueryHistory(QueryTimeWindow.ONE_HOUR, "in-memory");
        assertThat(history.executions()).isNotEmpty();
        assertThat(history.executions().getFirst().provider()).isEqualTo("in-memory");
        assertThat(history.executions().getFirst().query()).isEqualTo("software");
        assertThat(((Number) history.aggregatedMetrics().get("totalQueries")).intValue()).isGreaterThanOrEqualTo(1);

        var marketAnalysis = queryHistoryService.getMarketAnalysis("br", "software");
        assertThat(((Number) marketAnalysis.marketMetrics().get("marketSize")).intValue()).isGreaterThanOrEqualTo(1);
        assertThat(marketAnalysis.competitors()).containsKey("MCP Flow Software");

        assertThat(providerRoutingService.getProviderHealth()).extracting("provider").contains("in-memory");
        assertThat(queryHistoryMcpResources.getQueryHistory("1h", "in-memory")).contains("software");
    }

    private Long existingIcpId() {
        return icpDataService.findAllICPs().stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No ICP seeded for integration test"))
            .id();
    }
}
