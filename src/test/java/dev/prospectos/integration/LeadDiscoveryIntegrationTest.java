package dev.prospectos.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.prospectos.api.ICPDataService;
import dev.prospectos.api.dto.LeadDiscoveryRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "test-pg"})
@TestPropertySource(properties = "prospectos.leads.allowed-sources=in-memory,llm-discovery")
class LeadDiscoveryIntegrationTest extends PostgresIntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ICPDataService icpDataService;

    @Test
    void leadDiscovery_ReturnsResultsFromLlmDiscoverySource() throws Exception {
        LeadDiscoveryRequest request = new LeadDiscoveryRequest(
            "fornecedores de alimentos no interior do parana",
            "SUPPLIER",
            3,
            List.of("llm-discovery"),
            existingIcpId()
        );

        mockMvc.perform(post("/api/leads/discover")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("COMPLETED"))
            .andExpect(jsonPath("$.leads").isArray())
            .andExpect(jsonPath("$.leads.length()").value(1))
            .andExpect(jsonPath("$.leads[0].candidate.name").value("Acme Foods"))
            .andExpect(jsonPath("$.leads[0].candidate.website").value("https://acmefoods.com"))
            .andExpect(jsonPath("$.leads[0].source.sourceName").value("llm-discovery"))
            .andExpect(jsonPath("$.leads[0].leadKey").isNotEmpty());
    }

    @Test
    void leadDiscovery_RejectsDisallowedSource() throws Exception {
        LeadDiscoveryRequest request = new LeadDiscoveryRequest(
            "fornecedores de alimentos",
            "SUPPLIER",
            1,
            List.of("unknown-source"),
            existingIcpId()
        );

        mockMvc.perform(post("/api/leads/discover")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").isNotEmpty());
    }

    @Test
    void leadDiscovery_ReturnsBadRequestWhenIcpNotFound() throws Exception {
        LeadDiscoveryRequest request = new LeadDiscoveryRequest(
            "fornecedores de alimentos",
            "SUPPLIER",
            1,
            List.of("llm-discovery"),
            999L
        );

        mockMvc.perform(post("/api/leads/discover")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("ICP not found with id: 999"));
    }

    @Test
    void leadDiscovery_ValidatesBlankQuery() throws Exception {
        LeadDiscoveryRequest request = new LeadDiscoveryRequest(
            " ",
            "SUPPLIER",
            1,
            List.of("llm-discovery"),
            existingIcpId()
        );

        mockMvc.perform(post("/api/leads/discover")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").isNotEmpty());
    }

    private Long existingIcpId() {
        return icpDataService.findAllICPs().stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No ICP seeded for integration test"))
            .id();
    }
}
