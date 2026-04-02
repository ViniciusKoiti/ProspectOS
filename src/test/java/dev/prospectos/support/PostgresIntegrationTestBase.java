package dev.prospectos.support;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;

public abstract class PostgresIntegrationTestBase {

    @ServiceConnection
    protected static final PostgreSQLContainer<?> postgres = SharedPostgresContainer.instance();
}
