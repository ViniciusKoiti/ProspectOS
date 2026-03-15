package dev.prospectos.integration;

import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.prospectos.api.dto.request.CompanyCreateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CompanyListingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listCompanies_AppliesAdvancedFilters() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        long firstId = createCompany("BackendFilter Alpha " + suffix, "FilterIndustry", "Sao Paulo");
        long secondId = createCompany("BackendFilter Beta " + suffix, "FilterIndustry", "Sao Paulo");
        createCompany("BackendFilter Gamma " + suffix, "OtherIndustry", "Curitiba");

        updateScore(firstId, 88);
        updateScore(secondId, 62);

        mockMvc.perform(get("/api/companies")
                .param("query", suffix)
                .param("industry", "filterindustry")
                .param("location", "sao paulo")
                .param("minScore", "80")
                .param("maxScore", "90")
                .param("hasContact", "false"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(firstId))
            .andExpect(jsonPath("$[0].name").value("BackendFilter Alpha " + suffix))
            .andExpect(header().string("X-Total-Count", "1"));
    }

    @Test
    void listCompanies_AppliesPaginationAndValidatesPaginationParams() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        createCompany("BackendPage A " + suffix, "PageIndustry", "Sao Paulo");
        createCompany("BackendPage B " + suffix, "PageIndustry", "Sao Paulo");
        createCompany("BackendPage C " + suffix, "PageIndustry", "Sao Paulo");

        mockMvc.perform(get("/api/companies")
                .param("query", suffix)
                .param("page", "0")
                .param("size", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(header().string("X-Total-Count", "3"));

        mockMvc.perform(get("/api/companies")
                .param("query", suffix)
                .param("page", "1")
                .param("size", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(header().string("X-Total-Count", "3"));

        mockMvc.perform(get("/api/companies")
                .param("page", "-1")
                .param("size", "10"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").exists());

        mockMvc.perform(get("/api/companies")
                .param("page", "0")
                .param("size", "0"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").exists());
    }

    private long createCompany(String name, String industry, String city) throws Exception {
        CompanyCreateRequest createRequest = new CompanyCreateRequest(
            name,
            industry,
            "https://" + UUID.randomUUID() + ".example.com",
            "Company for listing integration tests",
            "BR",
            city,
            "SMALL"
        );
        String response = mockMvc.perform(post("/api/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private void updateScore(long companyId, int score) throws Exception {
        String scoreBody = """
            {
              "value": %d,
              "category": "WARM",
              "reasoning": "score set for listing tests"
            }
            """.formatted(score);
        mockMvc.perform(put("/api/companies/{companyId}/score", companyId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(scoreBody))
            .andExpect(status().isNoContent());
    }
}
