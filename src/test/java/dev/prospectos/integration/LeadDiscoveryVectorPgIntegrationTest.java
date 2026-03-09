package dev.prospectos.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.prospectos.api.CompanyDataService;
import dev.prospectos.api.dto.LeadDiscoveryRequest;
import dev.prospectos.api.dto.request.CompanyCreateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@TestPropertySource(properties = {
    "spring.autoconfigure.exclude=org.springframework.ai.model.openai.autoconfigure.OpenAiAudioSpeechAutoConfiguration,org.springframework.ai.model.openai.autoconfigure.OpenAiAudioTranscriptionAutoConfiguration,org.springframework.ai.model.openai.autoconfigure.OpenAiChatAutoConfiguration,org.springframework.ai.model.openai.autoconfigure.OpenAiEmbeddingAutoConfiguration,org.springframework.ai.model.openai.autoconfigure.OpenAiImageAutoConfiguration,org.springframework.ai.model.openai.autoconfigure.OpenAiModerationAutoConfiguration,org.springframework.ai.model.anthropic.autoconfigure.AnthropicChatAutoConfiguration",
    "prospectos.leads.allowed-sources=in-memory,vector-company",
    "prospectos.vectorization.backend=pgvector",
    "prospectos.vectorization.embedding-dimension=64",
    "spring.ai.vectorstore.type=pgvector",
    "spring.ai.vectorstore.pgvector.enabled=true",
    "spring.ai.vectorstore.pgvector.initialize-schema=true",
    "spring.ai.vectorstore.pgvector.table-name=company_vectors",
    "spring.ai.vectorstore.pgvector.dimensions=64"
})
class LeadDiscoveryVectorPgIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("pgvector/pgvector:pg16")
        .withDatabaseName("prospectos")
        .withUsername("prospectos")
        .withPassword("prospectos")
        .withInitScript("sql/pgvector-init.sql");

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
    }

    @Autowired
    private CompanyDataService companyDataService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void leadDiscovery_WithPgVectorBackend_ReturnsIndexedCompany() throws Exception {
        companyDataService.createCompany(new CompanyCreateRequest(
            "Vector Agile Labs",
            "Software",
            "https://vectoragilelabs.com",
            "Scrum and cloud software engineering consultancy",
            "BR",
            "Sao Paulo",
            "MEDIUM"
        ));

        LeadDiscoveryRequest request = new LeadDiscoveryRequest(
            "agile software cloud teams",
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
            .andExpect(jsonPath("$.leads[0]").exists())
            .andExpect(jsonPath("$.leads[0].source.sourceName").value("vector-company"))
            .andExpect(jsonPath("$.leads[0].candidate.name").value("Vector Agile Labs"));
    }
}
