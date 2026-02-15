# TD-018: Discovery e Scoring - Hardening de Arquitetura e Performance

**Status:** DONE  
**Priority:** P1 (HIGH)  
**Created:** 2026-02-15  
**Modules:** `infrastructure.service.discovery`, `infrastructure.service.jpa`, `infrastructure.service.leads`, `ai.client`

## Contexto

A primeira versao do endpoint `POST /api/leads/discover` foi entregue com arquitetura orientada a objetos (strategy por source + orquestrador).  
O fluxo esta funcional e com testes de integracao, mas ha debitos tecnicos para tornar producao mais previsivel, performatica e extensivel.

## Plano de atividades (separado)

- [x] **TD-018.1** lookup por `externalId` com query direta (Company + ICP)
- [x] **TD-018.2** deduplicacao no accept por website/dominio sem scan completo
- [x] **TD-018.3** source validada sem strategy registrada deve falhar com erro claro
- [x] **TD-018.4** habilitacao de source por property em vez de profile
- [x] **TD-018.5** converter com schema tipado + observabilidade de parse failure
- [x] **TD-018.6** hardening de configuracao H2 em development

## Progresso executado

### 2026-02-15 - TD-018.1 concluido

**Sequencia aplicada (teste primeiro, depois correcao):**
1. testes unitarios para garantir lookup direto por `externalId` e impedir `findAll()` no fluxo critico;
2. implementacao com `externalId` persistido em `Company` e repositorios/servicos ajustados.

**Arquivos principais:**
- `src/main/java/dev/prospectos/core/domain/Company.java`
- `src/main/java/dev/prospectos/core/repository/CompanyDomainRepository.java`
- `src/main/java/dev/prospectos/infrastructure/jpa/CompanyJpaRepository.java`
- `src/main/java/dev/prospectos/infrastructure/adapter/CompanyRepositoryAdapter.java`
- `src/main/java/dev/prospectos/infrastructure/service/jpa/CompanyDataServiceJpa.java`
- `src/test/java/dev/prospectos/infrastructure/service/jpa/CompanyDataServiceJpaTest.java`

### 2026-02-15 - TD-018.2 concluido

**Sequencia aplicada (teste primeiro, depois correcao):**
1. testes do `LeadAcceptService` criados primeiro para expor:
- falha de dedupe quando muda o formato da URL (`https://acme.com` vs `http://www.acme.com`);
- dependencia de scan completo com `findAllCompanies()`;
2. correcao com lookup dedicado `CompanyDataService.findByWebsite(String)` usando dominio normalizado.

**Arquivos principais:**
- `src/main/java/dev/prospectos/api/CompanyDataService.java`
- `src/main/java/dev/prospectos/infrastructure/service/leads/LeadAcceptService.java`
- `src/main/java/dev/prospectos/infrastructure/service/jpa/CompanyDataServiceJpa.java`
- `src/main/java/dev/prospectos/infrastructure/service/inmemory/InMemoryCompanyDataService.java`
- `src/test/java/dev/prospectos/infrastructure/service/leads/LeadAcceptServiceTest.java`

### 2026-02-15 - TD-018.3 concluido

**Sequencia aplicada (teste primeiro, depois correcao):**
1. teste unitario exigindo erro quando uma source validada nao possui strategy registrada;
2. ajuste no orquestrador para falha rapida com mensagem explicita em vez de ignorar silenciosamente.

**Arquivos principais:**
- `src/main/java/dev/prospectos/infrastructure/service/discovery/DefaultLeadDiscoveryService.java`
- `src/test/java/dev/prospectos/infrastructure/service/discovery/DefaultLeadDiscoveryServiceTest.java`

### 2026-02-15 - TD-018.4 concluido

**Sequencia aplicada (teste primeiro, depois correcao):**
1. teste de integracao de discovery executado sem override manual de `LeadDiscoverySource` no profile `test` (falhando no estado anterior);
2. substituicao de `@Profile(\"!test\")` por `@ConditionalOnProperty` na source LLM;
3. criacao de source in-memory padrao para profile `test`, eliminando dependencia de `@TestConfiguration` para o fluxo principal.

**Arquivos principais:**
- `src/main/java/dev/prospectos/infrastructure/service/discovery/LlmLeadDiscoverySource.java`
- `src/main/java/dev/prospectos/infrastructure/service/discovery/InMemoryLeadDiscoverySource.java`
- `src/main/resources/application-development.properties`
- `src/test/resources/application-test.properties`
- `src/test/java/dev/prospectos/integration/LeadDiscoveryIntegrationTest.java`

### 2026-02-15 - TD-018.5 concluido

**Sequencia aplicada (teste primeiro, depois correcao):**
1. testes do `LlmDiscoveryResponseConverter` exigindo erro explicito para JSON/schema invalidos (falhando no estado anterior);
2. troca de parse dinamico por schema tipado (`record`) para resposta de discovery;
3. inclusao de log estruturado e metrica interna de parse-failure por source.

**Arquivos principais:**
- `src/main/java/dev/prospectos/infrastructure/service/discovery/LlmDiscoveryResponseConverter.java`
- `src/test/java/dev/prospectos/infrastructure/service/discovery/LlmDiscoveryResponseConverterTest.java`

### 2026-02-15 - TD-018.6 concluido

**Sequencia aplicada (teste primeiro, depois correcao):**
1. testes de configuracao para garantir H2 estavel em `development` e em `.env.example` (falhando no estado anterior);
2. ajuste de URL H2 com `DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`;
3. ajuste de `ddl-auto` para `update` em desenvolvimento.

**Arquivos principais:**
- `src/main/resources/application-development.properties`
- `.env.example`
- `src/test/java/dev/prospectos/infrastructure/config/DevelopmentDatabaseConfigurationTest.java`

## Debitos tecnicos identificados

### 1) Lookup externo de Company ainda e `findAll + filter` (escalabilidade)

**Evidencia**
- `src/main/java/dev/prospectos/infrastructure/service/jpa/CompanyDataServiceJpa.java`
  - `findCompanyByExternalId(...)` usava `companyRepository.findAll().stream().filter(...)`
  - `findICPByExternalId(...)` usava `icpRepository.findAll().stream().filter(...)`

**Impacto**
- Complexidade O(n) por consulta.
- Crescimento de latencia com volume de dados.
- Acesso desnecessario a memoria.

**Abordagem recomendada**
- Persistir `externalId` tambem em `Company`.
- Criar queries diretas:
  - `CompanyJpaRepository.findByExternalId(Long)`
  - usar `ICPDomainRepository.findByExternalId(Long)` no lugar do filtro em memoria.

---

### 2) Deduplicacao no accept usa scan completo de empresas (O(n))

**Evidencia**
- `src/main/java/dev/prospectos/infrastructure/service/leads/LeadAcceptService.java`
  - `findExistingCompanyByWebsite(...)` percorria `findAllCompanies()`.

**Impacto**
- Gargalo em ambientes com muitas companies.
- Maior risco de timeout no fluxo de accept.

**Abordagem recomendada**
- Criar metodo no contrato e adapter:
  - `CompanyDataService.findByWebsite(String website)`
- Indexar website/domain no banco.
- Deduplicar por dominio normalizado (nao so URL literal).

---

### 3) Discovery ignora source sem strategy registrada (comportamento silencioso)

**Evidencia**
- `src/main/java/dev/prospectos/infrastructure/service/discovery/DefaultLeadDiscoveryService.java`
  - loop de sources faz `if (source == null) continue;`

**Impacto**
- Pode retornar vazio sem explicar causa.
- Dificulta troubleshooting e observabilidade.

**Abordagem recomendada**
- Falhar rapido quando source validada nao tem implementation:
  - `IllegalArgumentException("Configured source without implementation: ...")`
- Alternativa: registrar warning + `message` explicita na resposta.

---

### 4) Dependencia forte de profile para estrategias (acoplamento de ambiente)

**Evidencia**
- `src/main/java/dev/prospectos/infrastructure/service/discovery/LlmLeadDiscoverySource.java`
  - `@Profile("!test")` exige override de bean em teste.

**Impacto**
- Testes de integracao precisam de configuracao artificial.
- Menor previsibilidade entre ambientes.

**Abordagem recomendada**
- Preferir `@ConditionalOnProperty` para habilitar fontes.
- Exemplo:
  - `prospectos.discovery.llm.enabled=true/false`
- Criar `InMemoryLeadDiscoverySource` padrao para `test`.

---

### 5) Converter de discovery sem contrato forte de schema

**Evidencia**
- `src/main/java/dev/prospectos/infrastructure/service/discovery/LlmDiscoveryResponseConverter.java`
  - parse via `Map<String,Object>` sem schema tipado.
  - retorna silenciosamente `List.of()` em multiplas falhas.

**Impacto**
- Erros de parse mascarados como "sem resultados".
- Dificulta auditoria de qualidade por provider/modelo.

**Abordagem recomendada**
- Introduzir DTO interno tipado de resposta LLM (record).
- Log estruturado em falhas de parse com `requestId/source`.
- Metrica de taxa de parse-failure por source.

---

### 6) Configuracao H2 em dev pode causar banco "vazio" apos restart

**Evidencia**
- Logs mostraram erro: `Table "ICP" not found (this database is empty)`.
- `.env` atual usa `jdbc:h2:mem:prospectos` com `create-drop`.

**Impacto**
- Ambiente de desenvolvimento instavel.
- Falhas intermitentes e perda de schema.

**Abordagem recomendada**
- Em dev:
  - `jdbc:h2:mem:prospectos;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`
  - ou `jdbc:h2:file:./data/prospectos`
  - `spring.jpa.hibernate.ddl-auto=update`

## Prioridade de execucao sugerida

1. **P0/P1 imediato**
- TD-018.1 (lookup por externalId com query direta)
- TD-018.2 (dedupe por website com query indexada)
- TD-018.3 (source sem strategy nao pode falhar silencioso)

2. **P1**
- TD-018.4 (habilitacao por property ao inves de profile)
- TD-018.5 (schema tipado + observabilidade do converter)

3. **P2**
- TD-018.6 (padronizacao definitiva de ambiente dev H2)

## Criterios de aceite por item

- **018.1 / 018.2**: nenhum `findAll().stream().filter(...)` para lookup de ID externo/website no fluxo critico.
- **018.3**: source validada sem bean de strategy retorna erro claro.
- **018.4**: testes nao dependem de override manual para strategy principal.
- **018.5**: parse failures geram log e metrica, nao apenas lista vazia.
- **018.6**: restart em dev nao perde schema inesperadamente.
