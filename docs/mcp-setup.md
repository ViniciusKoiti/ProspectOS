# MCP Setup - ProspectOS

## Estado atual

Os prompts 1, 2 e 3 agora estao cobertos no backend:
- profile MCP em `src/main/resources/application-mcp.yml`
- health indicator dedicado em `/actuator/health/mcpServer`
- contratos MCP em `dev.prospectos.api.mcp`
- DTOs e mock services em `dev.prospectos.infrastructure.mcp`
- tool `get_query_metrics`

## Dependencias

O projeto usa as dependencias Spring AI MCP abaixo no `build.gradle`:
```gradle
implementation 'org.springframework.ai:spring-ai-starter-mcp-server'
implementation 'org.springframework.ai:spring-ai-starter-mcp-server-webmvc'
```

Observacao: no Spring AI 1.1.2 usado aqui, as annotations MCP ainda vem do pacote legado `org.springaicommunity.mcp.annotation.*`.

## Como iniciar

Para subir o servidor MCP via HTTP streamable:
```bash
./gradlew bootRun --args="--spring.profiles.active=development,mcp"
```

Se quiser usar apenas o profile MCP e sua base real ja estiver disponivel:
```bash
./gradlew bootRun --args="--spring.profiles.active=mcp"
```

## Configuracao ativa

O arquivo `application-mcp.yml` habilita:
- porta HTTP `8082`
- endpoint MCP streamable em `/mcp`
- suporte a `stdio=true`
- logs DEBUG para `org.springframework.ai.mcp` e `dev.prospectos.infrastructure.mcp`
- grupo de health `mcp`

## Validacao rapida

### Health check
```bash
curl http://localhost:8082/actuator/health/mcpServer
curl http://localhost:8082/actuator/health/mcp
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

### Aplicacao nao sobe com `mcp`
Causas mais provaveis:
- banco configurado no `application.properties` nao esta disponivel
- profile MCP foi iniciado sem o profile `development`

Fallback local recomendado:
```bash
./gradlew bootRun --args="--spring.profiles.active=development,mcp"
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