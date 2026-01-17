package dev.prospectos.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.prospectos.api.dto.request.ICPCreateRequest;
import dev.prospectos.api.dto.request.ICPUpdateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ICPManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void icpCrud_FlowWorksWithInMemoryStore() throws Exception {
        ICPCreateRequest createRequest = new ICPCreateRequest(
            "Growth ICP",
            "Targets growth-stage companies",
            List.of("Software", "SaaS"),
            List.of("LATAM"),
            List.of("CTO", "Head of Growth"),
            "Growth signals",
            List.of(),
            null,
            null
        );

        String createResponse = mockMvc.perform(post("/api/icps")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.name").value("Growth ICP"))
            .andReturn()
            .getResponse()
            .getContentAsString();

        long icpId = objectMapper.readTree(createResponse).get("id").asLong();
        assertThat(icpId).isPositive();

        mockMvc.perform(get("/api/icps/{icpId}", icpId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Growth ICP"));

        ICPUpdateRequest updateRequest = new ICPUpdateRequest(
            "Growth ICP Updated",
            "Updated description",
            List.of("Technology"),
            List.of("Global"),
            List.of("VP Sales"),
            "Updated theme",
            List.of(),
            null,
            null
        );

        mockMvc.perform(put("/api/icps/{icpId}", icpId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Growth ICP Updated"));

        mockMvc.perform(delete("/api/icps/{icpId}", icpId))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/icps/{icpId}", icpId))
            .andExpect(status().isNotFound());
    }

    @Test
    void listCompaniesByIcp_ReturnsSeededCompanies() throws Exception {
        mockMvc.perform(get("/api/icps/{icpId}/companies", 1L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").isNumber())
            .andExpect(jsonPath("$[0].id").isNumber());
    }
}
