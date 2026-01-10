package dev.prospectos.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.prospectos.api.dto.ProspectEnrichRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "file:.env")
@ActiveProfiles("test")
class ProspectEnrichmentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void enrichProspect_ReturnsEnrichedResponse() throws Exception {
        ProspectEnrichRequest request = new ProspectEnrichRequest(
            "Acme",
            "https://acme.com",
            "Technology"
        );

        String responseBody = mockMvc.perform(post("/api/prospect/enrich")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Example Company"))
            .andExpect(jsonPath("$.website").value("https://acme.com"))
            .andExpect(jsonPath("$.industry").value("Technology"))
            .andExpect(jsonPath("$.analysis").isNotEmpty())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThat(responseBody).contains("Example Company");
    }

    @Test
    void enrichProspect_RequiresNameAndWebsite() throws Exception {
        ProspectEnrichRequest request = new ProspectEnrichRequest(
            "",
            "",
            null
        );

        mockMvc.perform(post("/api/prospect/enrich")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").isNotEmpty());
    }
}
