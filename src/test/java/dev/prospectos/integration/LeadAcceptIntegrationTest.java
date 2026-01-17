package dev.prospectos.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.prospectos.api.dto.CompanyCandidateDTO;
import dev.prospectos.api.dto.ScoreDTO;
import dev.prospectos.api.dto.SourceProvenanceDTO;
import dev.prospectos.api.dto.request.AcceptLeadRequest;
import dev.prospectos.core.util.LeadKeyGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LeadAcceptIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void acceptLead_CreatesNewCompany() throws Exception {
        CompanyCandidateDTO candidate = new CompanyCandidateDTO(
            "NewTech Corp",
            "https://newtech.io",
            "Software",
            "Innovative software company",
            "MEDIUM",
            "San Francisco, CA",
            List.of("contact@newtech.io")
        );

        ScoreDTO score = new ScoreDTO(85, "HOT", "Great fit for ICP");

        SourceProvenanceDTO source = new SourceProvenanceDTO(
            "apollo",
            "https://newtech.io",
            Instant.now()
        );

        String leadKey = LeadKeyGenerator.generate("https://newtech.io", "apollo");

        AcceptLeadRequest request = new AcceptLeadRequest(leadKey, candidate, score, source);

        mockMvc.perform(post("/api/leads/accept")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.company").exists())
            .andExpect(jsonPath("$.company.id").exists())
            .andExpect(jsonPath("$.company.name").value("NewTech Corp"))
            .andExpect(jsonPath("$.company.website").value("https://newtech.io"))
            .andExpect(jsonPath("$.company.score.value").value(85))
            .andExpect(jsonPath("$.company.score.category").value("HOT"))
            .andExpect(jsonPath("$.message").value("Lead accepted and created"));
    }

    @Test
    void acceptLead_RejectsInvalidLeadKey() throws Exception {
        CompanyCandidateDTO candidate = new CompanyCandidateDTO(
            "Test Corp",
            "https://test.com",
            "Software",
            "Test company",
            null,
            null,
            List.of()
        );

        ScoreDTO score = new ScoreDTO(50, "WARM", "Good match");

        SourceProvenanceDTO source = new SourceProvenanceDTO(
            "in-memory",
            "https://test.com",
            Instant.now()
        );

        AcceptLeadRequest request = new AcceptLeadRequest("invalid-key", candidate, score, source);

        mockMvc.perform(post("/api/leads/accept")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Invalid leadKey format"));
    }

    @Test
    void acceptLead_SanitizesScore() throws Exception {
        CompanyCandidateDTO candidate = new CompanyCandidateDTO(
            "Score Test Corp",
            "https://scoretest.com",
            "Software",
            "Test score sanitization",
            null,
            null,
            List.of()
        );

        // Score out of bounds
        ScoreDTO score = new ScoreDTO(150, "UNKNOWN_CATEGORY", "Test");

        SourceProvenanceDTO source = new SourceProvenanceDTO(
            "test",
            "https://scoretest.com",
            Instant.now()
        );

        String leadKey = LeadKeyGenerator.generate("https://scoretest.com", "test");

        AcceptLeadRequest request = new AcceptLeadRequest(leadKey, candidate, score, source);

        mockMvc.perform(post("/api/leads/accept")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.company.score.value").value(100))  // clamped
            .andExpect(jsonPath("$.company.score.category").value("COLD"));  // normalized
    }

    @Test
    void acceptLead_UpdatesExistingCompany() throws Exception {
        // First accept
        CompanyCandidateDTO candidate1 = new CompanyCandidateDTO(
            "Existing Corp",
            "https://existing.com",
            "Software",
            "Original description",
            "SMALL",
            null,
            List.of()
        );

        ScoreDTO score1 = new ScoreDTO(50, "COLD", "Initial score");
        SourceProvenanceDTO source1 = new SourceProvenanceDTO("test", "https://existing.com", Instant.now());
        String leadKey1 = LeadKeyGenerator.generate("https://existing.com", "test");
        AcceptLeadRequest request1 = new AcceptLeadRequest(leadKey1, candidate1, score1, source1);

        mockMvc.perform(post("/api/leads/accept")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
            .andExpect(status().isOk());

        // Second accept with same website but different source - should update
        CompanyCandidateDTO candidate2 = new CompanyCandidateDTO(
            "Existing Corp",
            "https://existing.com",
            "Software",
            "Updated description",
            "MEDIUM",
            null,
            List.of()
        );

        ScoreDTO score2 = new ScoreDTO(90, "HOT", "Updated score");
        SourceProvenanceDTO source2 = new SourceProvenanceDTO("apollo", "https://existing.com", Instant.now());
        String leadKey2 = LeadKeyGenerator.generate("https://existing.com", "apollo");
        AcceptLeadRequest request2 = new AcceptLeadRequest(leadKey2, candidate2, score2, source2);

        mockMvc.perform(post("/api/leads/accept")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.company.score.value").value(90))
            .andExpect(jsonPath("$.company.score.category").value("HOT"))
            .andExpect(jsonPath("$.message").value("Lead accepted and updated (already existed)"));
    }
}
