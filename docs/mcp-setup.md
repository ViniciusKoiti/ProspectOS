# MCP Setup - ProspectOS

## Estado atual

Os prompts 1, 2 e 3 agora estao cobertos no backend:
- MCP ativado por `spring.ai.mcp.server.enabled`
- health indicator dedicado em `/actuator/health/mcpServer`
- contratos MCP em `dev.prospectos.api.mcp`
- DTOs e runtime MCP em `dev.prospectos.infrastructure.mcp`
- tool `get_query_metrics`

## Dependencias

O projeto usa as dependencias Spring AI MCP abaixo no `build.gradle`:
```gradle
implementation 'org.springframework.ai:spring-ai-starter-mcp-server'
implementation 'org.springframework.ai:spring-ai-starter-mcp-server-webmvc'
```

Observacao: no Spring AI 1.1.2 usado aqui, as annotations MCP ainda vem do pacote legado `org.springaicommunity.mcp.annotation.*`.

## Como iniciar

Para subir o servidor MCP via HTTP streamable no runtime local:
```bash
./gradlew bootRun --args="--spring.profiles.active=development --spring.ai.mcp.server.enabled=true"
```

## Configuracao ativa

As propriedades base agora habilitam:
- endpoint MCP streamable em `/mcp`
- seguranca HTTP via API key e rate limit
- grupo de health `mcp` quando `MANAGEMENT_ENDPOINT_HEALTH_GROUP_MCP_INCLUDE=mcpServer` estiver configurado
- logs DEBUG em `development` para `org.springframework.ai.mcp` e `dev.prospectos.infrastructure.mcp`

## Validacao rapida

### Health check
```bash
curl http://localhost:8080/actuator/health/mcp # opcional, requer MANAGEMENT_ENDPOINT_HEALTH_GROUP_MCP_INCLUDE=mcpServerServer
curl http://localhost:8080/actuator/health/mcp # opcional, requer MANAGEMENT_ENDPOINT_HEALTH_GROUP_MCP_INCLUDE=mcpServer
```

### MCP HTTP
O transporte HTTP atual usa endpoint streamable em `/mcp`.
Nao espere um endpoint REST legado como `/mcp/tools/list`.

## Troubleshooting

### Erro de classes MCP nao encontradas
Verifique se o Gradle baixou os artefatos MCP:
```bash
./gradlew compileJava
```

### Aplicacao nao sobe com MCP habilitado
Causas mais provaveis:
- banco configurado no `application.properties` nao esta disponivel
- MCP foi habilitado sem um profile de ambiente valido

Fallback local recomendado:
```bash
./gradlew bootRun --args="--spring.profiles.active=development --spring.ai.mcp.server.enabled=true"
```

### Tool nao aparece
Verifique:
- classe anotada com `@McpTool`
- bean registrado no contexto Spring
- endpoint de health retornando `toolCount` maior que zero

### Validacao de testes usada nesta implementacao
```bash
./gradlew test -x jacocoTestReport -x jacocoTestCoverageVerification \
  --tests 'dev.prospectos.api.mcp.QueryTimeWindowTest' \
  --tests 'dev.prospectos.api.mcp.RoutingStrategyTest' \
  --tests 'dev.prospectos.infrastructure.mcp.service.MockQueryMetricsServiceTest' \
  --tests 'dev.prospectos.infrastructure.mcp.service.MockProviderRoutingServiceTest' \
  --tests 'dev.prospectos.infrastructure.mcp.dto.McpDtoMappingsTest' \
  --tests 'dev.prospectos.infrastructure.mcp.tools.QueryMetricsMcpToolsTest' \
  --tests 'dev.prospectos.infrastructure.mcp.config.McpServerHealthIndicatorTest'
```

