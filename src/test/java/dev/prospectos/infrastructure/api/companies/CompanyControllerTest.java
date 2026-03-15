package dev.prospectos.infrastructure.api.companies;

import dev.prospectos.api.CompanyDataService;
import dev.prospectos.api.dto.CompanyContactDTO;
import dev.prospectos.api.dto.CompanyDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CompanyControllerTest {

    @Mock
    private CompanyDataService companyDataService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new CompanyController(companyDataService)).build();
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
}
