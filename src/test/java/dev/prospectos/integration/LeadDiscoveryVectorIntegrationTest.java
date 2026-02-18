package dev.prospectos.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.prospectos.api.dto.LeadDiscoveryRequest;
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
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "prospectos.leads.allowed-sources=in-memory,vector-company",
    "prospectos.discovery.vector.enabled=true",
    "prospectos.vectorization.min-similarity=0.05"
})
class LeadDiscoveryVectorIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void leadDiscovery_ReturnsResultsFromVectorSource() throws Exception {
        LeadDiscoveryRequest request = new LeadDiscoveryRequest(
            "software startups with cloud teams",
            "SUPPLIER",
            3,
            List.of("vector-company"),
            null
        );

        mockMvc.perform(post("/api/leads/discover")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("COMPLETED"))
            .andExpect(jsonPath("$.leads").isArray())
            .andExpect(jsonPath("$.leads[0]").exists())
            .andExpect(jsonPath("$.leads[0].source.sourceName").value("vector-company"))
            .andExpect(jsonPath("$.leads[0].leadKey").isNotEmpty())
            .andExpect(jsonPath("$.leads[0].candidate.website").isNotEmpty());
    }
}
