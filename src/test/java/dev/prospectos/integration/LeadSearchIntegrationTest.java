package dev.prospectos.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.prospectos.api.ICPDataService;
import dev.prospectos.api.dto.LeadSearchRequest;
import dev.prospectos.support.PostgresIntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "test-pg"})
class LeadSearchIntegrationTest extends PostgresIntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ICPDataService icpDataService;

    @Test
    void leadSearch_ReturnsResultsFromInMemorySource() throws Exception {
        LeadSearchRequest request = new LeadSearchRequest(
            "software",
            3,
            List.of("in-memory"),
            existingIcpId()
        );

        mockMvc.perform(post("/api/leads/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("COMPLETED"))
            .andExpect(jsonPath("$.leads").isArray())
            .andExpect(jsonPath("$.leads.length()", greaterThan(0)))
            .andExpect(jsonPath("$.leads.length()", lessThanOrEqualTo(3)))
            .andExpect(jsonPath("$.leads[0].candidate").exists())
            .andExpect(jsonPath("$.leads[0].candidate.name").exists())
            .andExpect(jsonPath("$.leads[0].candidate.website").exists())
            .andExpect(jsonPath("$.leads[0].leadKey").isNotEmpty())
            .andExpect(jsonPath("$.leads[0].score").exists())
            .andExpect(jsonPath("$.leads[0].source.sourceName").value("in-memory"));
    }

    @Test
    void leadSearch_RejectsDisallowedSource() throws Exception {
        LeadSearchRequest request = new LeadSearchRequest(
            "software",
            1,
            List.of("unknown"),
            existingIcpId()
        );

        mockMvc.perform(post("/api/leads/search")
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
