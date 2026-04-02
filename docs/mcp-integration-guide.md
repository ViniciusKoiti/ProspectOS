# MCP Integration Guide - ProspectOS

## Visao geral

O MCP expoe ferramentas e recursos operacionais do ProspectOS no mesmo processo Spring Boot.
A ativacao agora e feita por propriedade, nao por profile dedicado.

## Como habilitar localmente

Use o ambiente `development` e ligue o MCP por propriedade:

```bash
./gradlew bootRun --args="--spring.profiles.active=development --spring.ai.mcp.server.enabled=true"
```

## Endpoints relevantes

- MCP streamable HTTP: `http://localhost:8080/mcp`
- Health individual: `http://localhost:8080/actuator/health/mcpServer`
- Health group: `http://localhost:8080/actuator/health/mcp`

## Seguranca

A superficie MCP usa:
- API key via header `X-MCP-API-KEY`
- rate limit por cliente
- auditoria de autenticacao e throttling

## Validacao recomendada

### Teste de fluxo MCP com Postgres
```bash
./gradlew --no-daemon test --tests 'dev.prospectos.integration.McpPostgresFlowIntegrationTest'
```

Esse teste usa:
- `test + test-pg`
- `spring.ai.mcp.server.enabled=true`
- Postgres real via Testcontainers

### Teste de fluxo development com Postgres
```bash
./gradlew --no-daemon test --tests 'dev.prospectos.integration.LeadSearchDevelopmentPostgresIntegrationTest'
```

## Notas de runtime

- `development` agora usa o datasource Postgres compartilhado da configuracao base.
- O profile `mcp` deixou de ser necessario para subir o MCP.
- O runtime mock legado do MCP fica desabilitado por padrao via `prospectos.mcp.mock-runtime.enabled=false`.

## Proximo passo

Use o [setup detalhado](./mcp-setup.md) e a [estrategia de testes](./docs/mcp/testing-strategy.md) para validar a superficie completa.
