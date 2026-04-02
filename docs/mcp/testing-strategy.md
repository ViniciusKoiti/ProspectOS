# MCP Testing Strategy

## Scope

O slice MCP agora e validado em quatro camadas:

1. contratos em `src/test/java/dev/prospectos/api/mcp`
2. adapters MCP em `src/test/java/dev/prospectos/infrastructure/mcp`
3. fluxo MCP com Postgres/Testcontainers em `src/test/java/dev/prospectos/integration/McpPostgresFlowIntegrationTest.java`
4. fluxo `development` com Postgres/Testcontainers em `src/test/java/dev/prospectos/integration/LeadSearchDevelopmentPostgresIntegrationTest.java`

## Automated test matrix

### Contract tests
- `RoutingStrategyTest`
- `QueryTimeWindowTest`
- `QueryMetricsRecorderTest`

### Tool and resource tests
- `QueryMetricsMcpToolsTest`
- `ProviderRoutingMcpToolsTest`
- `QueryHistoryMcpResourcesTest`

### Configuration and security tests
- `McpServerHealthIndicatorTest`
- `McpApiKeyAuthenticationFilterTest`
- `McpRateLimitingFilterTest`

### Persistence and service tests
- `JpaQueryMetricsServiceTest`
- `QueryMetricsObservationEntityTest`
- `DefaultQueryHistoryServiceTest`
- `DefaultProviderRoutingServiceTest`

### Integration tests
- `McpPostgresFlowIntegrationTest`
- `LeadSearchDevelopmentPostgresIntegrationTest`

## Recommended commands

### Fast MCP iteration
```powershell
$env:GRADLE_USER_HOME='D:\Cursos\prospectos\.gradle-user-home'
./gradlew test --tests 'dev.prospectos.api.mcp.*' --tests 'dev.prospectos.infrastructure.mcp.*' -x jacocoTestReport -x jacocoTestCoverageVerification
```

### MCP flow with Postgres
```powershell
$env:GRADLE_USER_HOME='D:\Cursos\prospectos\.gradle-user-home'
./gradlew --no-daemon test --tests 'dev.prospectos.integration.McpPostgresFlowIntegrationTest' -x jacocoTestReport -x jacocoTestCoverageVerification
```

Observacao: esse teste ativa MCP por `spring.ai.mcp.server.enabled=true`, sem profile extra.

### Development flow with Postgres
```powershell
$env:GRADLE_USER_HOME='D:\Cursos\prospectos\.gradle-user-home'
./gradlew --no-daemon test --tests 'dev.prospectos.integration.LeadSearchDevelopmentPostgresIntegrationTest' -x jacocoTestReport -x jacocoTestCoverageVerification
```

### Full validation
```powershell
$env:GRADLE_USER_HOME='D:\Cursos\prospectos\.gradle-user-home'
./gradlew --no-daemon clean test
```

## Full-flow reference

Para o fluxo completo e a ordem de validacao:
- [full-flow-testing.md](/D:/Cursos/prospectos/docs/mcp/full-flow-testing.md)
- [mcp-full-runtime.puml](/D:/Cursos/prospectos/docs/mcp/flows/mcp-full-runtime.puml)
- [mcp-full-validation.puml](/D:/Cursos/prospectos/docs/mcp/flows/mcp-full-validation.puml)

## Security checks

A superficie HTTP do MCP continua protegida por filtros servlet em `/mcp/*`:
- API key via `X-MCP-API-KEY` ou `Authorization: Bearer ...`
- rate limiting por cliente com `429`
- auditoria de autenticacao e throttling

Os testes automatizados cobrem os caminhos de sucesso e rejeicao.
