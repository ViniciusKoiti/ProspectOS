package dev.prospectos.modules;

import dev.prospectos.ProspectosApplication;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModule;
import org.springframework.modulith.core.ApplicationModuleDependencies;
import org.springframework.modulith.core.ApplicationModules;
import static org.assertj.core.api.Assertions.assertThat;


public class ModulithTest {

    @Test
    void modulesRespectBoundaries() {
        ApplicationModules modules = ApplicationModules.of(ProspectosApplication.class);
        
        var coreModule = modules.getModuleByName("core").orElseThrow();
        var aiModule = modules.getModuleByName("ai").orElseThrow();
        
        // Core should have no dependencies (key architectural constraint)
        assertThat(coreModule.getDirectDependencies(modules).stream().count()).isEqualTo(0);
        assertThat(aiModule.getDependencies(modules).contains(coreModule)).isTrue();
    }

    @Test
    void coreIsDependent(){
        ApplicationModules modules = ApplicationModules.of(ProspectosApplication.class);

        ApplicationModule core = modules.getModuleByName("core")
                .orElseThrow();

        ApplicationModuleDependencies deps = core.getDirectDependencies(modules);

        assertThat(deps.stream().count()).isEqualTo(0);
    }
}
