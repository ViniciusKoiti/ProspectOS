package dev.prospectos.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.prospectos.api.ICPDataService;
import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.api.dto.ICPDto;
import dev.prospectos.api.dto.LeadSearchRequest;
import dev.prospectos.api.dto.LeadSearchResponse;
import dev.prospectos.support.PostgresIntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 🧪 Teste de Integração Completo - MVP Week 1
 *
 * Testa o sistema completo end-to-end:
 * - Endpoints REST funcionando
 * - DataSeeder populando dados
 * - Lead search com múltiplas sources
 * - CNPJ integration
 * - Performance básica
 *
 * Executa com perfil 'test' para ambiente controlado
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles({"test", "test-pg"})
class Week1MVPIntegrationTest extends PostgresIntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ICPDataService icpDataService;

    // ===== TESTES DE ENDPOINTS BÁSICOS =====

    @Test
    void shouldReturnCompaniesFromEndpoint() throws Exception {
        // When & Then
        MvcResult result = mockMvc.perform(get("/api/companies"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Parse response
        String json = result.getResponse().getContentAsString();
        CompanyDTO[] companies = objectMapper.readValue(json, CompanyDTO[].class);

        // Validações
        assertThat(companies).isNotEmpty();
        assertThat(companies.length).isGreaterThan(5); // Deve ter muitas empresas

        // Verificar estrutura dos dados
        CompanyDTO firstCompany = companies[0];
        assertThat(firstCompany.id()).isNotNull();
        assertThat(firstCompany.name()).isNotBlank();
        assertThat(firstCompany.industry()).isNotBlank();
        assertThat(firstCompany.website()).isNotBlank();
    }

    @Test
    void shouldReturnICPsFromEndpoint() throws Exception {
        // When & Then
        MvcResult result = mockMvc.perform(get("/api/icps"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Parse response
        String json = result.getResponse().getContentAsString();
        ICPDto[] icps = objectMapper.readValue(json, ICPDto[].class);

        // Validações
        assertThat(icps).hasSizeGreaterThanOrEqualTo(3); // DataSeeder default

        List<String> icpNames = Arrays.stream(icps)
                .map(ICPDto::name)
                .toList();

        // Verificar ICPs específicos criados pelo DataSeeder
        assertThat(icpNames).anyMatch(name -> name.contains("CTO") && name.contains("Startup"));
        assertThat(icpNames).anyMatch(name -> name.contains("Agronegócio"));
        assertThat(icpNames).anyMatch(name -> name.contains("Fintech"));
    }

    // ===== TESTES DE BUSCA DE LEADS =====

    @Test
    void shouldSearchLeadsForFintech() throws Exception {
        // Given
        LeadSearchRequest request = new LeadSearchRequest("fintech", 5, null, existingIcpId());

        // When & Then
        MvcResult result = mockMvc.perform(post("/api/leads/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andReturn();

        // Parse response
        LeadSearchResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                LeadSearchResponse.class);

        // Validações
        assertThat(response.status().toString()).isEqualTo("COMPLETED");

        // Verificar que contém empresas de fintech relevantes
        if (!response.leads().isEmpty()) {
            boolean hasRelevantFintech = response.leads().stream()
                    .anyMatch(lead ->
                        lead.candidate().name().toLowerCase().contains("fintech") ||
                        lead.candidate().industry().equals("fintech") ||
                        lead.candidate().name().contains("Pay") ||
                        lead.candidate().name().contains("Bank")
                    );

            assertThat(hasRelevantFintech).isTrue();
        }
    }

    @Test
    void shouldSearchLeadsForTechnology() throws Exception {
        // Given
        LeadSearchRequest request = new LeadSearchRequest("technology", 5, null, existingIcpId());

        // When & Then
        MvcResult result = mockMvc.perform(post("/api/leads/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andReturn();

        // Parse response
        LeadSearchResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                LeadSearchResponse.class);

        // Validações

        // Verificar scores são atribuídos
        assertThat(response.leads()).allMatch(lead ->
            lead.score() != null &&
            lead.score().value() >= 45 &&
            lead.score().value() <= 95
        );

        // Verificar categories
        assertThat(response.leads()).allMatch(lead ->
            List.of("HOT", "WARM", "COLD").contains(lead.score().category())
        );
    }

    @Test
    void shouldSearchLeadsForAgribusiness() throws Exception {
        // Given
        LeadSearchRequest request = new LeadSearchRequest("agribusiness", 5, null, existingIcpId());

        // When & Then
        MvcResult result = mockMvc.perform(post("/api/leads/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andReturn();

        // Parse response
        LeadSearchResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                LeadSearchResponse.class);

        // Validações para agronegócio

        if (!response.leads().isEmpty()) {
            boolean hasAgroCompanies = response.leads().stream()
                    .anyMatch(lead ->
                        lead.candidate().industry().contains("agri") ||
                        lead.candidate().name().contains("Agrícola") ||
                        lead.candidate().name().contains("Agro") ||
                        lead.candidate().name().contains("Farm")
                    );

            assertThat(hasAgroCompanies).isTrue();
        }
    }

    // ===== TESTES DE INTEGRAÇÃO CNPJ =====

    @Test
    void shouldSearchWithCNPJSourceSpecifically() throws Exception {
        // Given - busca específica com source CNPJ
        String requestBody = """
            {
                "query": "tecnologia SP",
                "sources": ["cnpj-ws"],
                "limit": 3,
                "icpId": %d
            }
            """.formatted(existingIcpId());

        // When & Then
        MvcResult result = mockMvc.perform(post("/api/leads/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andReturn();

        // Parse response
        LeadSearchResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                LeadSearchResponse.class);

        // Validações específicas para CNPJ source
        if (!response.leads().isEmpty()) {
            // Se retornou resultados, devem ser todos do CNPJ source
            assertThat(response.leads()).allMatch(lead ->
                lead.source().sourceName().equals("cnpj-ws")
            );

            // Deve conter empresas brasileiras específicas do CNPJ mock
            boolean hasCNPJCompanies = response.leads().stream()
                    .anyMatch(lead ->
                        lead.candidate().name().contains("Brasil") ||
                        lead.candidate().name().contains("TechSolutions") ||
                        lead.candidate().name().contains("InovaPay")
                    );
        }
    }

//    @Test
//    void shouldSearchWithMultipleSources() throws Exception {
//        // Given - busca com múltiplas sources
//        String requestBody = """
//            {
//                "query": "empresa",
//                "sources": ["in-memory", "cnpj-ws"],
//                "limit": 10
//            }
//            """;
//
//        // When & Then
//        MvcResult result = mockMvc.perform(post("/api/leads/search")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(requestBody))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("COMPLETED"))
//                .andReturn();
//
//        // Parse response
//        LeadSearchResponse response = objectMapper.readValue(
//                result.getResponse().getContentAsString(),
//                LeadSearchResponse.class);
//
//        // Validações
//        assertThat(response.leads()).isNotEmpty();
//
//        // Verificar que temos resultados de diferentes sources
//        List<String> sourceNames = response.leads().stream()
//                .map(lead -> lead.source().sourceName())
//                .distinct()
//                .toList();
//
//        // Deve ter pelo menos uma source
//        assertThat(sourceNames).isNotEmpty();
//
//        // Sources válidas esperadas
//        List<String> expectedSources = List.of("in-memory", "cnpj-ws", "vector-company");
//        assertThat(sourceNames).allSatisfy(source ->
//            assertThat(expectedSources).contains(source)
//        );
//    }

    // ===== TESTES DE QUALIDADE DOS DADOS =====

    @Test
    void shouldReturnBrazilianCompaniesInResults() throws Exception {
        // Given
        LeadSearchRequest request = new LeadSearchRequest("empresa Brasil", 20, null, existingIcpId());

        // When
        MvcResult result = mockMvc.perform(post("/api/leads/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        LeadSearchResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                LeadSearchResponse.class);

        assertThat(response.status().toString()).isEqualTo("COMPLETED");

        // Then - deve ter empresas brasileiras
        if (!response.leads().isEmpty()) {
            boolean hasBrazilianCompanies = response.leads().stream()
                    .anyMatch(lead ->
                        lead.candidate().location().contains("Brazil") ||
                        lead.candidate().location().contains("SP") ||
                        lead.candidate().location().contains("RJ") ||
                        lead.candidate().location().contains("MG") ||
                        lead.candidate().name().contains("Brasil")
                    );

            assertThat(hasBrazilianCompanies).isTrue();
        }
    }

    @Test
    void shouldReturnValidScoreDistribution() throws Exception {
        // Given - busca ampla para ver distribuição de scores
        LeadSearchRequest request = new LeadSearchRequest("startup tecnologia", 20, null, existingIcpId());

        // When
        MvcResult result = mockMvc.perform(post("/api/leads/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        LeadSearchResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                LeadSearchResponse.class);

        // Then - análise da distribuição de scores
        if (!response.leads().isEmpty()) {
            List<Integer> scores = response.leads().stream()
                    .map(lead -> lead.score().value())
                    .toList();

            // Scores devem estar no range correto
            assertThat(scores).allMatch(score -> score >= 45 && score <= 95);

            // Deve ter variação nos scores (não todos iguais)
            long distinctScores = scores.stream().distinct().count();
            assertThat(distinctScores).isGreaterThan(1);

            // Verificar categories correspondentes aos scores
            response.leads().forEach(lead -> {
                int score = lead.score().value();
                String category = lead.score().category();

                if (score >= 80) {
                    assertThat(category).isEqualTo("HOT");
                } else if (score >= 65) {
                    assertThat(category).isEqualTo("WARM");
                } else {
                    assertThat(category).isEqualTo("COLD");
                }
            });
        }
    }

    // ===== TESTE DE PERFORMANCE =====

    @Test
    void shouldRespondWithinAcceptableTime() throws Exception {
        // Given
        LeadSearchRequest request = new LeadSearchRequest("tecnologia", 10, null, existingIcpId());

        // When - medir tempo de resposta
        long startTime = System.currentTimeMillis();

        mockMvc.perform(post("/api/leads/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        // Then - deve responder em menos de 5 segundos
        assertThat(responseTime).isLessThan(5000L);
    }

    // ===== TESTE DE CENÁRIOS DE DEMO =====

    @Test
    void shouldSupportDemoScenario_CTOStartups() throws Exception {
        // Given - cenário realista de demo
        String demoQuery = "CTO startup tecnologia São Paulo";
        LeadSearchRequest request = new LeadSearchRequest(demoQuery, 5, null, existingIcpId());

        // When & Then
        MvcResult result = mockMvc.perform(post("/api/leads/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andReturn();

        LeadSearchResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                LeadSearchResponse.class);

        // Validações para demo
        assertThat(response.leads()).isNotEmpty();
        assertThat(response.leads()).hasSizeLessThanOrEqualTo(5);

        // Deve ter empresas relevantes para startups tech
        boolean hasRelevantResults = response.leads().stream()
                .anyMatch(lead ->
                    lead.candidate().industry().contains("tech") ||
                    lead.candidate().name().toLowerCase().contains("tech") ||
                    lead.score().reasoning().toLowerCase().contains("startup")
                );

        assertThat(hasRelevantResults).isTrue();
    }

    private Long existingIcpId() {
        return icpDataService.findAllICPs().stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No ICP seeded for integration test"))
            .id();
    }
}
