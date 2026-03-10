# Execucao: Migracao para `@ServiceConnection` (10 Etapas)

## Objetivo
Substituir configuracao manual de Testcontainers por `@ServiceConnection` com baixo risco, mantendo estabilidade da suite e paridade com PostgreSQL real.

## Resultado atual (2026-03-10)
- Etapas 1, 2, 3, 4, 5, 6 e 7: concluidas.
- Etapa 8: concluida para o escopo atual.
- Etapas 9 e 10: concluidas para o escopo dos testes migrados.

## Etapa 1 - Inventario dos testes de integracao

| Classe | Banco/persistencia | Testcontainers | `@DynamicPropertySource` | Status de migracao |
|---|---|---|---|---|
| `LeadDiscoveryVectorPgIntegrationTest` | PostgreSQL (pgvector) | Sim | Sim | Migrado para `@ServiceConnection` |
| `CompanyDataServiceJpaDevelopmentIntegrationTest` | JPA | Nao | Nao | Migrado para perfil `test-pg` + container base |
| `ICPDataServiceJpaDevelopmentIntegrationTest` | JPA | Nao | Nao | Migrado para perfil `test-pg` + container base |
| `CompanyManagementIntegrationTest` | HTTP + persistencia | Nao | Nao | Migrado para `test-pg` + container base |
| `ICPManagementIntegrationTest` | HTTP + persistencia | Nao | Nao | Migrado para `test-pg` + container base |
| `LeadAcceptIntegrationTest` | HTTP + persistencia | Nao | Nao | Migrado para `test-pg` + container base |
| `LeadSearchIntegrationTest` | HTTP + busca de leads | Nao | Nao | Migrado para `test-pg` + container base |
| `LeadDiscoveryIntegrationTest` | HTTP + discovery de leads | Nao | Nao | Migrado para `test-pg` + container base |
| `LeadDiscoveryVectorIntegrationTest` | HTTP + discovery vetorial | Nao | Nao | Migrado para `test-pg` + container base |
| `ProspectEnrichmentIntegrationTest` | HTTP + enrichment | Nao | Nao | Migrado para `test-pg` + container base |
| `LeadSearchScraperIntegrationTest` | HTTP + scraper flow | Nao | Nao | Migrado para `test-pg` + container base |
| `Week1MVPIntegrationTest` | HTTP + fluxo E2E MVP | Nao | Nao | Migrado para `test-pg` + container base |
| `ScoringPersistenceIntegrationTest` | Servico + persistencia de score | Nao | Nao | Migrado para `test-pg` + container base |
| `ProspectingWorkflowIntegrationTest` | Workflow de prospeccao ponta a ponta | Nao | Nao | Migrado para `test-pg` + container base |
| `AIProvidersIntegrationTest` | Sem dependencia de DB containerizada | Nao | Sim | Mantido (uso para dotenv/chaves) |
| Demais `*IntegrationTest` com `@ActiveProfiles("test")` | In-memory/H2 (legado) | Nao | Nao | Mantidos para migracao em lotes seguintes |

## Etapa 2 - Politica de coexistencia H2 x PostgreSQL
- `test` continua como perfil legado para fluxos rapidos e componentes em memoria.
- `test-pg` passa a ser o perfil alvo para integracao/persistencia com PostgreSQL real.
- Novos testes que validam comportamento de persistencia real devem usar `test-pg` + `@ServiceConnection`.
- Ajuste de roteamento aplicado:
  - `CompanyDataServiceJpa` e `ICPDataServiceJpa` agora tambem ativos em `test-pg`.
  - `InMemoryCompanyDataService` e `InMemoryICPDataService` ficam desativados quando `test-pg` esta ativo.

## Etapa 3 - Preparar build para `@ServiceConnection`
- `build.gradle` ja estava correto com:
  - `testImplementation 'org.springframework.boot:spring-boot-testcontainers'`
  - `testImplementation 'org.testcontainers:junit-jupiter'`
  - `testImplementation 'org.testcontainers:postgresql'`
- Nenhuma mudanca adicional necessaria nesta etapa.

## Etapa 4 - Criar profile de teste PostgreSQL dedicado
- Criado: `src/test/resources/application-test-pg.properties`.
- Conteudo principal:
  - `spring.test.database.replace=none`
  - dialeto PostgreSQL
  - `ddl-auto=create-drop` (default para testes de persistencia)
  - `spring.datasource.hikari.maximum-pool-size=2`
  - `spring.datasource.hikari.minimum-idle=0`
  - exclusao de auto-configs OpenAI/Anthropic mantendo PgVector habilitavel

## Etapa 5 - Migrar teste piloto (`LeadDiscoveryVectorPgIntegrationTest`)
- Removido `@DynamicPropertySource`.
- Removido container local duplicado na classe.
- Classe agora estende `PostgresIntegrationTestBase`.
- Perfil ativo alterado para `@ActiveProfiles({"test", "test-pg"})`.
- Propriedades de datasource/credenciais/porta nao sao mais declaradas manualmente.

## Etapa 6 - Extrair base reutilizavel
- Criada `src/test/java/dev/prospectos/support/PostgresIntegrationTestBase.java` com:
  - `@ServiceConnection`
  - `PostgreSQLContainer` `pgvector/pgvector:pg16` + init script `sql/pgvector-init.sql`
  - inicializacao estatica singleton (`static { postgres.start(); }`) para evitar troca de URL entre classes e reduzir flakiness de lifecycle

## Etapa 7 - Migrar em lotes por prioridade tecnica
Lote 1 (repositorio/JPA) concluido:
- `CompanyDataServiceJpaDevelopmentIntegrationTest`
- `ICPDataServiceJpaDevelopmentIntegrationTest`

Lote 2 (HTTP com persistencia) concluido:
- `CompanyManagementIntegrationTest`
- `ICPManagementIntegrationTest`
- `LeadAcceptIntegrationTest`

Lote 3 (HTTP de busca/discovery/enrichment) concluido:
- `LeadSearchIntegrationTest`
- `LeadDiscoveryIntegrationTest`
- `LeadDiscoveryVectorIntegrationTest`
- `ProspectEnrichmentIntegrationTest`

Lote 4 (HTTP restantes) concluido:
- `LeadSearchScraperIntegrationTest`
- `Week1MVPIntegrationTest`

Lote 5 (servicos de fluxo/persistencia) concluido:
- `ScoringPersistenceIntegrationTest`
- `ProspectingWorkflowIntegrationTest`

Mudancas aplicadas no lote:
- heranca de `PostgresIntegrationTestBase`
- `@AutoConfigureTestDatabase(replace = NONE)`
- perfis ativos:
  - JPA slice: `@ActiveProfiles({"development", "test-pg"})`
  - HTTP integration: `@ActiveProfiles({"test", "test-pg"})`

## Etapa 8 - Endurecer testes de integracoes externas
- Sem regressao introduzida nesta execucao.
- Mecanismos atuais de mock/isolamento foram preservados.
- Ponto pendente para lotes futuros: revisar testes HTTP externos durante migracao dos proximos grupos.

## Etapa 9 - Validar pipeline (local + CI)
Validacao local executada (Docker ativo):
- `./gradlew test --tests 'dev.prospectos.integration.LeadSearchIntegrationTest' --info --stacktrace -x jacocoTestCoverageVerification -x jacocoTestReport` -> sucesso.
- Lote de regressao dos testes migrados (JPA + HTTP + workflow) -> sucesso.
- `./gradlew test --tests 'dev.prospectos.integration.*IntegrationTest' -x jacocoTestCoverageVerification -x jacocoTestReport` -> sucesso.

Observacao:
- Em ambiente sandbox, os testes precisaram de permissao para acesso ao pipe Docker (`\\.\pipe\docker_engine`).
- Com Docker acessivel, a migracao `@ServiceConnection` executa de forma estavel para os testes migrados.

## Etapa 10 - Limpeza final e padrao oficial
Concluida no escopo atual:
- Padrao `@ServiceConnection` foi estabelecido para os testes migrados.
- Fluxos criticos migrados para `test-pg` foram validados com PostgreSQL real.
- Testes legados nao migrados permanecem em `test` por decisao explicita de coexistencia.

## Proximos lotes recomendados
1. Consolidar cenarios de pgvector que hoje ainda usam combinacao parcial de perfil legado.
2. Avaliar migracao dos testes sem dependencia direta de DB conforme custo-beneficio (`AIServicesIntegrationTest`, `AIProvidersIntegrationTest`, `EnrichmentPipelineIntegrationTest`).
3. Definir gate de CI com Docker obrigatorio para testes `test-pg`.
