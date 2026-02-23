package dev.prospectos.modules;

import dev.prospectos.ProspectosApplication;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

import static org.assertj.core.api.Assertions.assertThat;

class ModulithTest {

    @Test
    void verifyModuleArchitecture() {
        ApplicationModules modules = ApplicationModules.of(ProspectosApplication.class);

        modules.verify();
    }

    @Test
    void coreHasNoDirectDependencies() {
        ApplicationModules modules = ApplicationModules.of(ProspectosApplication.class);

        var coreModule = modules.getModuleByName("core")
                .orElseThrow(() -> new AssertionError("Módulo 'core' não encontrado."));

        assertThat(coreModule.getDirectDependencies(modules))
                .as("O módulo 'core' não deve depender diretamente de nenhum outro módulo")
                .satisfies(dependencies -> assertThat(dependencies.stream().count())
                        .as("Dependências diretas encontradas para o módulo 'core'")
                        .isZero());
    }
}
