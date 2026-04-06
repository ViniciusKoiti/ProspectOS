package dev.prospectos.infrastructure.api.companies;

import dev.prospectos.api.CompanyDataService;
import dev.prospectos.api.dto.CompanyContactDTO;
import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.api.dto.ScoreDTO;
import dev.prospectos.infrastructure.handler.ApiExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CompanyControllerTest {

    @Mock
    private CompanyDataService companyDataService;

    private final CompanyListFilter companyListFilter = new CompanyListFilter();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new CompanyController(companyDataService, companyListFilter))
            .setControllerAdvice(new ApiExceptionHandler())
            .build();
    }

    @Test
    void getCompanyContacts_ReturnsContactsWhenCompanyExists() throws Exception {
        CompanyDTO company = new CompanyDTO(
            1L,
            "Acme",
            "Software",
            "https://acme.com",
            "Description",
            null,
            "Sao Paulo, BR",
            null,
            "alice@acme.com",
            1
        );
        CompanyContactDTO contact = new CompanyContactDTO("Alice", "alice@acme.com", "CTO", null);

        when(companyDataService.findCompany(1L)).thenReturn(company);
        when(companyDataService.findCompanyContacts(1L)).thenReturn(List.of(contact));

        mockMvc.perform(get("/api/companies/{companyId}/contacts", 1L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Alice"))
            .andExpect(jsonPath("$[0].email").value("alice@acme.com"))
            .andExpect(jsonPath("$[0].position").value("CTO"));
    }

    @Test
    void getCompanyContacts_ReturnsNotFoundWhenCompanyDoesNotExist() throws Exception {
        when(companyDataService.findCompany(99L)).thenReturn(null);

        mockMvc.perform(get("/api/companies/{companyId}/contacts", 99L))
            .andExpect(status().isNotFound());
    }

    @Test
    void listCompanies_AppliesAdvancedFiltersAndReturnsTotalCountHeader() throws Exception {
        CompanyDTO first = new CompanyDTO(
            1L,
            "BackendFilter Alpha",
            "Software",
            "https://alpha.example.com",
            "Alpha company",
            20,
            "Sao Paulo",
            new ScoreDTO(88, "HOT", "Strong fit"),
            null,
            0
        );
        CompanyDTO second = new CompanyDTO(
            2L,
            "BackendFilter Beta",
            "Software",
            "https://beta.example.com",
            "Beta company",
            20,
            "Sao Paulo",
            new ScoreDTO(62, "WARM", "Medium fit"),
            null,
            0
        );
        CompanyDTO third = new CompanyDTO(
            3L,
            "BackendFilter Gamma",
            "Manufacturing",
            "https://gamma.example.com",
            "Gamma company",
            20,
            "Curitiba",
            new ScoreDTO(91, "HOT", "Strong fit"),
            "owner@gamma.example.com",
            1
        );

        when(companyDataService.findAllCompanies()).thenReturn(List.of(first, second, third));

        mockMvc.perform(get("/api/companies")
                .param("query", "alpha")
                .param("industry", "software")
                .param("location", "sao paulo")
                .param("minScore", "80")
                .param("maxScore", "90")
                .param("hasContact", "false"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("BackendFilter Alpha"))
            .andExpect(jsonPath("$[0].score.value").value(88))
            .andExpect(header().string("X-Total-Count", "1"));
    }

    @Test
    void listCompanies_AppliesPaginationAndKeepsTotalCountHeader() throws Exception {
        CompanyDTO first = createCompany(1L, "BackendPage A");
        CompanyDTO second = createCompany(2L, "BackendPage B");
        CompanyDTO third = createCompany(3L, "BackendPage C");

        when(companyDataService.findAllCompanies()).thenReturn(List.of(first, second, third));

        mockMvc.perform(get("/api/companies")
                .param("query", "backendpage")
                .param("page", "1")
                .param("size", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(3))
            .andExpect(header().string("X-Total-Count", "3"));
    }

    @Test
    void listCompanies_ReturnsBadRequestWhenScoreRangeIsInvalid() throws Exception {
        when(companyDataService.findAllCompanies()).thenReturn(List.of());

        mockMvc.perform(get("/api/companies")
                .param("minScore", "90")
                .param("maxScore", "80"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void listCompanies_ReturnsBadRequestWhenPaginationIsInvalid() throws Exception {
        when(companyDataService.findAllCompanies()).thenReturn(List.of());

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

    private CompanyDTO createCompany(Long id, String name) {
        return new CompanyDTO(
            id,
            name,
            "PageIndustry",
            "https://" + name.toLowerCase().replace(" ", "-") + ".example.com",
            "Page test company",
            30,
            "Sao Paulo",
            null,
            null,
            0
        );
    }
}
