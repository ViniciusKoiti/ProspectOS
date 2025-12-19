package dev.prospectos.modules;

import dev.prospectos.ProspectosApplication;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;


public class ModulithTest {

    @Test
    void modulesRespectBoundaries() {
        ApplicationModules.of(ProspectosApplication.class)
                .verify();
    }
}
