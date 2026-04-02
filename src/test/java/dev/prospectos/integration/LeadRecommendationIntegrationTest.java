package dev.prospectos.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.prospectos.api.dto.LeadRecommendationRequest;
import dev.prospectos.api.mcp.QueryMetricsRecorder;
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
@TestPropertySource(properties = "prospectos.leads.allowed-sources=in-memory,google-places,amazon-location")
class LeadRecommendationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private QueryMetricsRecorder queryMetricsRecorder;

    @Test
    void recommendationEndpointUsesObservedMetricsForConfiguredProductionSources() throws Exception {
        queryMetricsRecorder.recordExecution("google-places", 220, true, 5);
        queryMetricsRecorder.recordExecution("amazon-location", 650, false, 0);

        mockMvc.perform(post("/api/leads/recommendation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LeadRecommendationRequest(
                    "dentists in orlando",
                    20,
                    List.of("google-places", "amazon-location", "in-memory"),
                    null,
                    "24h"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.recommendedSource").value("google-places"))
            .andExpect(jsonPath("$.fallbackSources[0]").value("amazon-location"))
            .andExpect(jsonPath("$.fallbackSources[1]").value("in-memory"))
            .andExpect(jsonPath("$.timeWindow").value("24h"));
    }
}
