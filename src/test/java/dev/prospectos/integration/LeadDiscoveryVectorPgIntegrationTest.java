package dev.prospectos.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.prospectos.api.CompanyDataService;
import dev.prospectos.api.dto.LeadDiscoveryRequest;
import dev.prospectos.api.dto.request.CompanyCreateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@TestPropertySource(properties = {
    "spring.autoconfigure.exclude=",
    "prospectos.leads.allowed-sources=in-memory,vector-company",
    "prospectos.discovery.vector.enabled=true",
    "prospectos.vectorization.backend=pgvector",
    "prospectos.vectorization.embedding-dimension=64",
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

    @TestConfiguration
    static class TestEmbeddingConfiguration {

        @Bean
        @Primary
        EmbeddingModel embeddingModel() {
            return new DeterministicEmbeddingModel(64);
        }
    }

    static class DeterministicEmbeddingModel implements EmbeddingModel {

        private final int dimensions;

        DeterministicEmbeddingModel(int dimensions) {
            this.dimensions = dimensions;
        }

        @Override
        public EmbeddingResponse call(EmbeddingRequest request) {
            List<Embedding> embeddings = new ArrayList<>();
            List<String> instructions = request == null ? List.of() : request.getInstructions();
            for (int i = 0; i < instructions.size(); i++) {
                embeddings.add(new Embedding(embedText(instructions.get(i)), i));
            }
            return new EmbeddingResponse(embeddings);
        }

        @Override
        public float[] embed(Document document) {
            return embedText(document == null ? "" : document.getContent());
        }

        private float[] embedText(String text) {
            float[] vector = new float[dimensions];
            if (text == null || text.isBlank()) {
                return vector;
            }

            String[] tokens = text.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}]+");
            for (String token : tokens) {
                if (token == null || token.isBlank()) {
                    continue;
                }
                int hash = token.hashCode();
                int index = Math.floorMod(hash, dimensions);
                vector[index] += ((Integer.rotateLeft(hash, 7) & 1) == 0) ? 1.0f : -1.0f;
            }
            return normalize(vector);
        }

        private float[] normalize(float[] vector) {
            double norm = 0.0d;
            for (float value : vector) {
                norm += value * value;
            }
            if (norm == 0.0d) {
                return vector;
            }
            float scale = (float) (1.0d / Math.sqrt(norm));
            for (int i = 0; i < vector.length; i++) {
                vector[i] = vector[i] * scale;
            }
            return vector;
        }
    }
}
