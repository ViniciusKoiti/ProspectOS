package dev.prospectos.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.prospectos.api.dto.request.CompanyCreateRequest;
import dev.prospectos.api.dto.request.CompanyUpdateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "file:.env")
@ActiveProfiles("test")
class CompanyManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void companyCrud_FlowWorksWithInMemoryStore() throws Exception {
        CompanyCreateRequest createRequest = new CompanyCreateRequest(
            "NewCo",
            "Technology",
            "https://newco.com",
            "New company",
            "BR",
            "Sao Paulo",
            "SMALL"
        );

        String createResponse = mockMvc.perform(post("/api/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.name").value("NewCo"))
            .andExpect(jsonPath("$.industry").value("Technology"))
            .andReturn()
            .getResponse()
            .getContentAsString();

        long companyId = objectMapper.readTree(createResponse).get("id").asLong();
        assertThat(companyId).isPositive();

        mockMvc.perform(get("/api/companies/{companyId}", companyId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("NewCo"));

        CompanyUpdateRequest updateRequest = new CompanyUpdateRequest(
            "NewCo Updated",
            "SaaS",
            "https://newco.com",
            "Updated description",
            "BR",
            "Rio",
            "MEDIUM"
        );

        mockMvc.perform(put("/api/companies/{companyId}", companyId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("NewCo Updated"))
            .andExpect(jsonPath("$.industry").value("SaaS"));

        mockMvc.perform(delete("/api/companies/{companyId}", companyId))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/companies/{companyId}", companyId))
            .andExpect(status().isNotFound());
    }
}
