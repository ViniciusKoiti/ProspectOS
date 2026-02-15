package dev.prospectos.infrastructure.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DevelopmentDatabaseConfigurationTest {

    @Test
    void developmentProfile_UsesStableH2Configuration() throws IOException {
        String developmentProps = Files.readString(Path.of("src/main/resources/application-development.properties"));

        assertTrue(
            developmentProps.contains("spring.datasource.url=jdbc:h2:mem:prospectos;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"),
            "development profile must keep H2 in-memory DB alive across restarts"
        );
        assertTrue(
            developmentProps.contains("spring.jpa.hibernate.ddl-auto=update"),
            "development profile must avoid create-drop to reduce schema loss risk"
        );
    }

    @Test
    void envExample_UsesStableH2ConfigurationForDevelopment() throws IOException {
        String envExample = Files.readString(Path.of(".env.example"));

        assertTrue(
            envExample.contains("SPRING_DATASOURCE_URL=jdbc:h2:mem:prospectos;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"),
            ".env.example should point to stable H2 memory URL for development"
        );
        assertTrue(
            envExample.contains("SPRING_JPA_HIBERNATE_DDL_AUTO=update"),
            ".env.example should recommend update for development schema stability"
        );
    }
}
