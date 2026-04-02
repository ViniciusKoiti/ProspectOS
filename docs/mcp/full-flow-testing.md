# MCP Full Flow Testing

## Objetivo

Validar o fluxo completo do MCP em quatro etapas:

1. gerar dados reais no backend
2. persistir observacoes MCP no Postgres
3. consultar tools/resources MCP sobre esses dados
4. verificar seguranca e comportamento operacional

## Arranjos recomendados

### MCP real
- `test + test-pg` com `spring.ai.mcp.server.enabled=true`
- usado em [McpPostgresFlowIntegrationTest.java](/D:/Cursos/prospectos/src/test/java/dev/prospectos/integration/McpPostgresFlowIntegrationTest.java)
- escolha esse arranjo quando a meta e validar o runtime MCP sem arrastar a stack inteira de AI do profile `development`

### Desenvolvimento com Postgres
- `development + test-pg`
- usado em [LeadSearchDevelopmentPostgresIntegrationTest.java](/D:/Cursos/prospectos/src/test/java/dev/prospectos/integration/LeadSearchDevelopmentPostgresIntegrationTest.java)
- escolha esse profile quando a meta e validar o fluxo normal de busca com wiring de desenvolvimento sobre Postgres real

## Fluxo completo a testar

1. subir Postgres via Testcontainers ou Docker local
2. inicializar a aplicacao com o arranjo alvo
3. criar ou carregar ICP e companies no banco
4. executar `POST /api/leads/search` ou `POST /api/leads/discover`
5. confirmar que a execucao gravou `mcp_query_observations`
6. consultar tools/resources MCP
7. validar autenticacao, rate limit e respostas

## O que deve ser assertado

### Geracao de dados
- a busca retorna `COMPLETED`
- ha pelo menos um lead vindo da source esperada

### Persistencia MCP
- `QueryMetricsService` retorna `totalQueries >= 1`
- `QueryHistoryService` retorna execucoes com provider e query corretos
- `DefaultProviderRoutingService` consegue calcular health com base nas observacoes persistidas

### Recursos MCP
- `query-history://{timeWindow}/{provider}` devolve payload coerente
- `provider-performance://{provider}/{metric}` devolve serie temporal coerente
- `market-analysis://{country}/{industry}` reflete os dados persistidos de companies

### Seguranca
- sem API key: `401`
- acima do rate limit: `429`
- actuator MCP: health `UP`

## Ordem recomendada de execucao

```powershell
$env:GRADLE_USER_HOME='D:\Cursos\prospectos\.gradle-user-home'
./gradlew test --tests 'dev.prospectos.infrastructure.mcp.*' -x jacocoTestReport -x jacocoTestCoverageVerification
./gradlew --no-daemon test --tests 'dev.prospectos.integration.McpPostgresFlowIntegrationTest' -x jacocoTestReport -x jacocoTestCoverageVerification
./gradlew --no-daemon test --tests 'dev.prospectos.integration.LeadSearchDevelopmentPostgresIntegrationTest' -x jacocoTestReport -x jacocoTestCoverageVerification
./gradlew --no-daemon clean test
```

## Diagramas

- runtime completo: [mcp-full-runtime.puml](/D:/Cursos/prospectos/docs/mcp/flows/mcp-full-runtime.puml)
- fluxo de validacao: [mcp-full-validation.puml](/D:/Cursos/prospectos/docs/mcp/flows/mcp-full-validation.puml)
