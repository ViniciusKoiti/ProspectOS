# TD-018: Discovery e Scoring - Hardening de Arquitetura e Performance

**Status:** OPEN  
**Priority:** P1 (HIGH)  
**Created:** 2026-02-15  
**Modules:** `infrastructure.service.discovery`, `infrastructure.service.jpa`, `infrastructure.service.leads`, `ai.client`

## Contexto

A primeira versão do endpoint `POST /api/leads/discover` foi entregue com arquitetura orientada a objetos (strategy por source + orquestrador).  
O fluxo está funcional e com testes de integração, mas há débitos técnicos claros para tornar produção mais previsível, performática e extensível.

## Débitos técnicos identificados

### 1) Lookup externo de Company ainda é `findAll + filter` (escalabilidade)

**Evidência**
- `src/main/java/dev/prospectos/infrastructure/service/jpa/CompanyDataServiceJpa.java`
  - `findCompanyByExternalId(...)` usa `companyRepository.findAll().stream().filter(...)`
  - `findICPByExternalId(...)` também usa `icpRepository.findAll().stream().filter(...)`

**Impacto**
- Complexidade O(n) por consulta.
- Crescimento de latência com volume de dados.
- Acesso desnecessário à memória.

**Abordagem recomendada**
- Persistir `externalId` também em `Company` (assim como feito em ICP).
- Criar queries diretas:
  - `CompanyJpaRepository.findByExternalId(Long)`
  - usar `ICPDomainRepository.findByExternalId(Long)` no lugar do filtro em memória.

---

### 2) Deduplicação no accept usa scan completo de empresas (O(n))

**Evidência**
- `src/main/java/dev/prospectos/infrastructure/service/leads/LeadAcceptService.java`
  - `findExistingCompanyByWebsite(...)` percorre `findAllCompanies()`.
  - Existem TODOs explícitos no arquivo sobre isso.

**Impacto**
- Gargalo em ambientes com muitas companies.
- Maior risco de timeout no fluxo de accept.

**Abordagem recomendada**
- Criar método no contrato e adapter:
  - `CompanyDataService.findByWebsite(String website)`
- Indexar website/domain no banco.
- Deduplicar por domínio normalizado (não só URL literal).

---

### 3) Discovery ignora source sem strategy registrada (comportamento silencioso)

**Evidência**
- `src/main/java/dev/prospectos/infrastructure/service/discovery/DefaultLeadDiscoveryService.java`
  - loop de sources faz `if (source == null) continue;`

**Impacto**
- Pode retornar vazio sem explicar causa.
- Dificulta troubleshooting e observabilidade.

**Abordagem recomendada**
- Falhar rápido quando source validada não tem implementation:
  - `IllegalArgumentException("Configured source without implementation: ...")`
- Alternativa: registrar warning + `message` explícita na resposta.

---

### 4) Dependência forte de profile para estratégias (acoplamento de ambiente)

**Evidência**
- `src/main/java/dev/prospectos/infrastructure/service/discovery/LlmLeadDiscoverySource.java`
  - `@Profile("!test")` exige override de bean em teste.

**Impacto**
- Testes de integração precisam de configuração artificial.
- Menor previsibilidade entre ambientes.

**Abordagem recomendada**
- Preferir `@ConditionalOnProperty` para habilitar fontes.
- Exemplo:
  - `prospectos.discovery.llm.enabled=true/false`
- Criar `InMemoryLeadDiscoverySource` padrão para `test`.

---

### 5) Converter de discovery sem contrato forte de schema

**Evidência**
- `src/main/java/dev/prospectos/infrastructure/service/discovery/LlmDiscoveryResponseConverter.java`
  - parse via `Map<String,Object>` sem schema tipado.
  - retorna silenciosamente `List.of()` em múltiplas falhas.

**Impacto**
- Erros de parse mascarados como “sem resultados”.
- Dificulta auditoria de qualidade por provider/modelo.

**Abordagem recomendada**
- Introduzir DTO interno tipado de resposta LLM (record).
- Log estruturado em falhas de parse com `requestId/source`.
- Métrica de taxa de parse-failure por source.

---

### 6) Configuração H2 em dev pode causar banco “vazio” após restart

**Evidência**
- Logs mostraram erro: `Table "ICP" not found (this database is empty)`.
- `.env` atual usa `jdbc:h2:mem:prospectos` com `create-drop`.

**Impacto**
- Ambiente de desenvolvimento instável.
- Falhas intermitentes e perda de schema.

**Abordagem recomendada**
- Em dev:
  - `jdbc:h2:mem:prospectos;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`
  - ou `jdbc:h2:file:./data/prospectos`
  - `spring.jpa.hibernate.ddl-auto=update`

## Prioridade de execução sugerida

1. **P0/P1 imediato**
- TD-018.1 (lookup por externalId com query direta)
- TD-018.2 (dedupe por website com query indexada)
- TD-018.3 (source sem strategy não pode falhar silencioso)

2. **P1**
- TD-018.4 (habilitação por property ao invés de profile)
- TD-018.5 (schema tipado + observabilidade do converter)

3. **P2**
- TD-018.6 (padronização definitiva de ambiente dev H2)

## Critérios de aceite por item

- **018.1 / 018.2**: nenhum `findAll().stream().filter(...)` para lookup de ID externo/website no fluxo crítico.
- **018.3**: source validada sem bean de strategy retorna erro claro.
- **018.4**: testes não dependem de override manual para strategy principal.
- **018.5**: parse failures geram log e métrica, não apenas lista vazia.
- **018.6**: restart em dev não perde schema inesperadamente.

