package dev.prospectos.infrastructure.config;

import dev.prospectos.api.CompanyDataService;
import dev.prospectos.api.ICPDataService;
import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.api.dto.ICPDto;
import dev.prospectos.api.dto.ScoreDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 🧪 Teste Unitário do DataSeeder
 * 
 * Valida que o DataSeeder:
 * - Cria ICPs corretamente
 * - Cria empresas com dados realistas  
 * - Atribui scores apropriados
 * - Funciona sem dependências externas
 */
@ExtendWith(MockitoExtension.class)
class DataSeederTest {

    @Mock
    private CompanyDataService companyService;

    @Mock
    private ICPDataService icpService;

    @InjectMocks
    private DataSeeder dataSeeder;

    @BeforeEach
    void setUp() {
        // Mock para simular database vazio inicialmente
        when(companyService.findAllCompanies()).thenReturn(List.of());
        when(icpService.findAllICPs()).thenReturn(List.of());
    }

    @Test
    void shouldCreateICPsWhenDatabaseIsEmpty() {
        // Given - database vazio
        when(companyService.findAllCompanies()).thenReturn(List.of());

        // When - executar seeding
        dataSeeder.seedDatabase();

        // Then - deve criar 3 ICPs
        verify(icpService, times(3)).createICP(any());
    }

    @Test
    void shouldCreateCompaniesWhenDatabaseIsEmpty() {
        // Given - database vazio
        when(companyService.findAllCompanies()).thenReturn(List.of());

        // When - executar seeding
        dataSeeder.seedDatabase();

        // Then - deve criar muitas empresas (pelo menos 30)
        verify(companyService, atLeast(30)).createCompany(any());
    }

    @Test
    void shouldAssignScoresAfterCreatingCompanies() {
        // Given - database com empresas mockadas
        CompanyDTO nubank = new CompanyDTO(1L, "Nubank", "fintech", "https://nubank.com.br",
            "Digital banking platform", 1000, "São Paulo, Brazil", null);
        CompanyDTO slc = new CompanyDTO(2L, "SLC Agrícola", "agribusiness", "https://slcagricola.com.br", 
            "Large scale farming", 500, "Primavera do Leste, MT, Brazil", null);
        
        when(companyService.findAllCompanies()).thenReturn(List.of(nubank, slc));

        // When - executar seeding
        dataSeeder.seedDatabase();

        // Then - deve atribuir scores para as empresas
        verify(companyService, times(2)).updateCompanyScore(any(), any(ScoreDTO.class));
    }

    @Test
    void shouldCreateTechStartupICP() {
        // When
        dataSeeder.seedDatabase();

        // Then - verificar se criou ICP de startups tech
        verify(icpService).createICP(argThat(icp -> 
            icp.name().contains("CTO") && 
            icp.name().contains("Startup") &&
            icp.industries().contains("technology")
        ));
    }

    @Test
    void shouldCreateAgroICP() {
        // When  
        dataSeeder.seedDatabase();

        // Then - verificar se criou ICP de agronegócio
        verify(icpService).createICP(argThat(icp ->
            icp.name().contains("Agronegócio") &&
            icp.industries().contains("agribusiness") &&
            icp.regions().contains("Mato Grosso")
        ));
    }

    @Test
    void shouldCreateFintechICP() {
        // When
        dataSeeder.seedDatabase();

        // Then - verificar se criou ICP de fintech
        verify(icpService).createICP(argThat(icp ->
            icp.name().contains("Fintech") &&
            icp.industries().contains("fintech") &&
            icp.targetRoles().contains("CEO")
        ));
    }

    @Test
    void shouldCreateBrazilianCompanies() {
        // When
        dataSeeder.seedDatabase();

        // Then - verificar se criou empresas brasileiras específicas
        verify(companyService).createCompany(argThat(company ->
            company.name().equals("Nubank") &&
            company.industry().equals("fintech") &&
            company.city().contains("São Paulo")
        ));

        verify(companyService).createCompany(argThat(company ->
            company.name().equals("SLC Agrícola") &&
            company.industry().equals("agribusiness") &&
            company.city().contains("Mato Grosso")
        ));
    }

    @Test  
    void shouldCreateDiverseIndustries() {
        // When
        dataSeeder.seedDatabase();

        // Then - verificar diversidade de indústrias
        verify(companyService).createCompany(argThat(company -> 
            company.industry().equals("fintech")));
        verify(companyService).createCompany(argThat(company -> 
            company.industry().equals("agribusiness")));  
        verify(companyService).createCompany(argThat(company -> 
            company.industry().equals("technology")));
        verify(companyService).createCompany(argThat(company -> 
            company.industry().equals("saas")));
    }
}