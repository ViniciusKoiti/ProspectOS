package dev.prospectos.integration;

import dev.prospectos.ProspectosApplication;
import dev.prospectos.core.domain.Company;
import dev.prospectos.core.domain.ICP;
import dev.prospectos.core.domain.Website;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.core.ApplicationModule;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ModulithBoundariesIntegrationTest {

    @Test
    void modulesRespectBoundaries() {
        ApplicationModules modules = ApplicationModules.of(ProspectosApplication.class);
        
        var coreModule = modules.getModuleByName("core").orElseThrow();
        var aiModule = modules.getModuleByName("ai").orElseThrow();
        
        assertThat(coreModule.getDirectDependencies(modules).stream().count()).isEqualTo(0);
        
        assertThat(aiModule.getDependencies(modules).contains(coreModule)).isTrue();
    }

    @Test
    void coreModuleHasZeroDependencies() {
        ApplicationModules modules = ApplicationModules.of(ProspectosApplication.class);
        ApplicationModule coreModule = modules.getModuleByName("core")
                .orElseThrow(() -> new AssertionError("Core module not found"));
        
        long dependencyCount = coreModule.getDirectDependencies(modules).stream().count();
        assertThat(dependencyCount).isEqualTo(0);
    }

    @Test
    void aiModuleDependsOnCore() {
        ApplicationModules modules = ApplicationModules.of(ProspectosApplication.class);
        ApplicationModule aiModule = modules.getModuleByName("ai")
                .orElseThrow(() -> new AssertionError("AI module not found"));
        ApplicationModule coreModule = modules.getModuleByName("core")
                .orElseThrow(() -> new AssertionError("Core module not found"));
        
        boolean dependsOnCore = aiModule.getDependencies(modules).contains(coreModule);
        assertThat(dependsOnCore).isTrue();
    }

    @Test
    void coreDomainsAccessibleAcrossModules() {
        assertThatCode(() -> {
            Company company = Company.create(
                "TestCorp", 
                Website.of("https://test.com"),
                "Technology"
            );
            
            ICP icp = ICP.create(
                "Test ICP",
                "Test description",
                List.of("Technology"),
                List.of("Global"),
                List.of("CTO"),
                "Test theme"
            );
            
            assertThat(company.getName()).isEqualTo("TestCorp");
            assertThat(icp.getInterestTheme()).isEqualTo("Test theme");
            
        }).doesNotThrowAnyException();
    }
}