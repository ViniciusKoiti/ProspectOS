package dev.prospectos.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.prospectos.api.CompanyDataService;
import dev.prospectos.api.ICPDataService;
import dev.prospectos.api.dto.LeadDiscoveryRequest;
import dev.prospectos.api.dto.request.CompanyCreateRequest;
import dev.prospectos.support.PostgresIntegrationTestBase;
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
@ActiveProfiles({"test", "test-pg"})
@TestPropertySource(properties = {
    "prospectos.leads.allowed-sources=in-memory,vector-company",
    "prospectos.vectorization.backend=pgvector",
    "prospectos.vectorization.embedding-dimension=64",
    "spring.ai.vectorstore.type=pgvector",
    "spring.ai.vectorstore.pgvector.enabled=true",
    "spring.ai.vectorstore.pgvector.initialize-schema=true",
    "spring.ai.vectorstore.pgvector.table-name=company_vectors",
    "spring.ai.vectorstore.pgvector.dimensions=64",
    "spring.jpa.hibernate.ddl-auto=none"
})
class LeadDiscoveryVectorPgIntegrationTest extends PostgresIntegrationTestBase {

    @Autowired
    private CompanyDataService companyDataService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ICPDataService icpDataService;

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
            existingIcpId()
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

    private Long existingIcpId() {
        return icpDataService.findAllICPs().stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No ICP seeded for integration test"))
            .id();
    }
}
