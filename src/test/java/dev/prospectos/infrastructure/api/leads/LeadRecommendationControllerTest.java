package dev.prospectos.infrastructure.api.leads;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.prospectos.api.LeadRecommendationService;
import dev.prospectos.api.dto.LeadRecommendationRequest;
import dev.prospectos.api.dto.LeadRecommendationResponse;
import dev.prospectos.infrastructure.handler.ApiExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LeadRecommendationControllerTest {

    @Mock
    private LeadRecommendationService leadRecommendationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new LeadRecommendationController(leadRecommendationService))
            .setControllerAdvice(new ApiExceptionHandler())
            .build();
    }

    @Test
    void recommendReturnsSerializedRecommendationPayload() throws Exception {
        when(leadRecommendationService.recommend(any(LeadRecommendationRequest.class))).thenReturn(new LeadRecommendationResponse(
            "google-places",
            List.of("amazon-location", "scraper"),
            "Selected google-places based on observed success rate 97% and avg response time 220ms over 24h.",
            new BigDecimal("0.03"),
            220L,
            "24h"
        ));

        mockMvc.perform(post("/api/leads/recommendation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(new LeadRecommendationRequest(
                    "dentists in orlando",
                    20,
                    List.of("google-places", "amazon-location"),
                    null,
                    null
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.recommendedSource").value("google-places"))
            .andExpect(jsonPath("$.fallbackSources[0]").value("amazon-location"))
            .andExpect(jsonPath("$.expectedLatencyMs").value(220))
            .andExpect(jsonPath("$.timeWindow").value("24h"));
    }
}
