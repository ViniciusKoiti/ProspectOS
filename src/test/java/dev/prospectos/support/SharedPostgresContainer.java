package dev.prospectos.support;

import org.testcontainers.containers.PostgreSQLContainer;

final class SharedPostgresContainer {

    private static final PostgreSQLContainer<?> INSTANCE = createAndStart();

    private SharedPostgresContainer() {
    }

    static PostgreSQLContainer<?> instance() {
        return INSTANCE;
    }

    private static PostgreSQLContainer<?> createAndStart() {
        PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("pgvector/pgvector:pg16")
            .withDatabaseName("prospectos")
            .withUsername("prospectos")
            .withPassword("prospectos")
            .withInitScript("sql/pgvector-init.sql");
        postgres.start();
        return postgres;
    }
}
