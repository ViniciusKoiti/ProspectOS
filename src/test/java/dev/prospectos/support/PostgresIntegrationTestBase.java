package dev.prospectos.support;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;

public abstract class PostgresIntegrationTestBase {

    @ServiceConnection
    protected static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("pgvector/pgvector:pg16")
        .withDatabaseName("prospectos")
        .withUsername("prospectos")
        .withPassword("prospectos")
        .withInitScript("sql/pgvector-init.sql");

    static {
        postgres.start();
    }
}
