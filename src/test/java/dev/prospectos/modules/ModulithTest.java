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
        ApplicationModules.of(ProspectosApplication.class)
                .verify();
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
