package dev.prospectos.infrastructure.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DevelopmentDatabaseConfigurationTest {

    @Test
    void developmentProfile_UsesSharedPostgresConfiguration() throws IOException {
        String developmentProps = Files.readString(Path.of("src/main/resources/application-development.properties"));

        assertTrue(
            !developmentProps.contains("spring.datasource.url="),
            "development profile should inherit datasource configuration from the shared base properties"
        );
        assertTrue(
            developmentProps.contains("spring.jpa.hibernate.ddl-auto=update"),
            "development profile must keep schema updates non-destructive for local runtime"
        );
    }

    @Test
    void envExample_UsesPostgresConfigurationForDevelopment() throws IOException {
        String envExample = Files.readString(Path.of(".env.example"));

        assertTrue(
            envExample.contains("SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/prospectos"),
            ".env.example should point to the shared PostgreSQL development runtime"
        );
        assertTrue(
            envExample.contains("SPRING_JPA_DATABASE_PLATFORM=org.hibernate.dialect.PostgreSQLDialect"),
            ".env.example should align development with the PostgreSQL dialect used by the real runtime"
        );
    }
}
