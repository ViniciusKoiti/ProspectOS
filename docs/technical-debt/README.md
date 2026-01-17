# Technical Debt Analysis - ProspectOS

Esta análise identificou 8 pontos de débito técnico no projeto ProspectOS, categorizados por severidade e impacto. O projeto demonstra boa arquitetura geral com Spring Modulith, mas possui alguns pontos críticos que precisam de atenção.

## Status Geral da Análise

**Última atualização**: 2026-01-17

- ✅ **RESOLVIDOS**: 6 itens (TD-003, TD-004, TD-005, TD-006, TD-007, TD-008)
- ❌ **PENDENTES**: 9 itens (TD-001, TD-002, TD-010, TD-011, TD-012, TD-013, TD-014, TD-015, TD-017)
- **Taxa de resolução**: 40% (6/15)

## Sumário por Severidade

- **🔴 CRITICAL (6)**: Itens que afetam estabilidade, segurança e funcionalidade core
- **🟠 HIGH (3)**: Itens que impactam operações e confiabilidade
- **🟡 MEDIUM (5)**: Itens que afetam qualidade e manutenibilidade
- **🔵 LOW (1)**: Itens de melhoria de qualidade

## Sumário por Categoria

### 🎯 Lead Flow (Preview → Accept) - P0
Débitos críticos que bloqueiam a funcionalidade principal do produto:
- **TD-010**: Lead search persiste durante preview (viola requisito)
- **TD-011**: Falta endpoint de aceite de lead
- **TD-012**: Inconsistência de tipo icpId (UUID vs Long)
- **TD-013**: Scoring acoplado a persistência
- **TD-014**: DTO inadequado para preview (tem id)
- **TD-015**: Falta ICP default
- **TD-017**: Mapeamento incompleto de ICP para scoring

### 🔐 Security & Build
- **TD-001**: Dependências SNAPSHOT/Milestone
- **TD-002**: `.env` commitado no repositório

## Lista Completa

| ID | Title | Severity | Type | Status | Arquivos |
|---|---|---|---|---|---|
| [TD-001](TD-001-build-snapshot-dependencies.md) | Build dependencies using SNAPSHOT and Milestone versions | 🔴 critical | build | ❌ OPEN | `build.gradle` |
| [TD-002](TD-002-committed-env-file.md) | Arquivo .env commitado no repositório com configurações sensíveis | 🔴 critical | security | ❌ OPEN | `.env`, `.gitignore` |
| [TD-003](TD-003-tests-depend-on-local-env.md) | Testes de integração dependem de variáveis de ambiente locais | 🟠 high | test | ✅ RESOLVED | ~~`integration/*Test.java`~~ |
| [TD-004](TD-004-system-out-production-code.md) | Uso de System.out em código de produção | 🟠 high | observability | ✅ RESOLVED | ~~`TokenUsageMonitor.java`~~ |
| [TD-005](TD-005-optional-orelse-null-antipattern.md) | Uso de Optional.orElse(null) violando práticas de Optional | 🟡 medium | design | ✅ RESOLVED | ~~`*DataServiceJpa.java`~~ |
| [TD-006](TD-006-missing-ai-integration-resilience.md) | Falta de timeouts e retry em integrações AI | 🟡 medium | reliability | ✅ RESOLVED | ~~`AIWebSearchScraperClient.java`~~ |
| [TD-007](TD-007-test-compilation-error.md) | Erro sintático em teste de integração de enrichment | 🟡 medium | test | ✅ RESOLVED | ~~`EnrichmentPipelineIntegrationTest.java`~~ |
| [TD-008](TD-008-duplicated-todomain-methods.md) | Duplicação de métodos toDomainCompany em múltiplas classes | 🔵 low | maintainability | ✅ RESOLVED | ~~`*Test.java`, `CompanyScoringService.java`~~ |
| [TD-010](TD-010-lead-search-persistence.md) | Lead Search persiste dados durante preview | 🔴 critical | product | ❌ OPEN | `ScraperLeadSearchService.java` |
| [TD-011](TD-011-lead-accept-endpoint.md) | Falta endpoint de aceite de lead | 🔴 critical | product | ❌ OPEN | `infrastructure.api.leads` |
| [TD-012](TD-012-icpid-type-mismatch.md) | LeadSearchRequest.icpId está como UUID (inconsistente) | 🔴 critical | contract | ❌ OPEN | `LeadSearchRequest.java` |
| [TD-013](TD-013-scoring-preview.md) | Scoring acoplado a companyId persistido | 🔴 critical | architecture | ❌ OPEN | `CompanyScoringService.java` |
| [TD-014](TD-014-company-dto-preview.md) | CompanyDTO inadequado para preview (tem id) | 🟠 high | api-model | ❌ OPEN | `LeadResultDTO.java`, `CompanyDTO.java` |
| [TD-015](TD-015-icp-default.md) | ICP default quando icpId não vier | 🟠 high | product | ❌ OPEN | `LeadSearchService` implementations |
| [TD-017](TD-017-icp-mapping-incomplete.md) | Mapeamento incompleto de ICP para scoring | 🟠 high | correctness | ❌ OPEN | `CompanyScoringService.java`, `ICPDto.java` |

## Recomendações de Priorização

### ✅ **Concluídos**
- ~~**TD-003**: Corrigir dependência de .env em testes~~ ✅ RESOLVIDO (2026-01-11)
- ~~**TD-004**: Implementar logging estruturado~~ ✅ RESOLVIDO (antes de 2026-01-11)
- ~~**TD-005**: Refatorar uso de Optional~~ ✅ RESOLVIDO (antes de 2026-01-11)
- ~~**TD-006**: Adicionar resilience patterns nas integrações AI~~ ✅ RESOLVIDO (2026-01-11)
- ~~**TD-007**: Fix compilação~~ ✅ RESOLVIDO (antes de 2026-01-11)
- ~~**TD-008**: Consolidar métodos de conversão DTO→Domain~~ ✅ RESOLVIDO (2026-01-11)

### 🎯 **Imediata (Prioridade 1) - Lead Flow**
Implementar fluxo completo "Preview → Accept" (bloqueador do produto):
1. **TD-012**: Alinhar icpId para Long (breaking change controlado)
2. **TD-014**: Criar CompanyCandidateDTO (sem id)
3. **TD-013**: Criar scoring preview (sem persistência)
4. **TD-015**: Implementar ICP default
5. **TD-010**: Remover persistência do lead search
6. **TD-011**: Criar endpoint de accept
7. **TD-017**: Completar mapeamento de ICP

### 🔥 **Alta Prioridade (Prioridade 2) - Security**
8. **TD-002**: Remover .env do repositório e regenerar secrets (CRITICAL)

### 📦 **Média Prioridade (Prioridade 3) - Build**
9. **TD-001**: Migrar para versões stable do Spring Boot e Spring AI (CRITICAL)

## Detalhes da Verificação (2026-01-11)

### ✅ Itens Resolvidos

**TD-003: Testes dependem de .env**
- ✅ Removida anotação `@TestPropertySource(locations = "file:.env")` de todos os testes
- ✅ Testes agora usam configuração via `application-test.properties`
- ✅ Todos os 11 arquivos de teste atualizados (AIProvidersIntegrationTest, EnrichmentPipelineIntegrationTest, etc.)
- ✅ Import não usado de `TestPropertySource` removido

**TD-004: System.out em código de produção**
- ✅ Nenhuma ocorrência de `System.out.` encontrada em `src/main/**/*.java`
- TokenUsageMonitor.java foi refatorado para usar logging apropriado

**TD-005: Optional.orElse(null) antipattern**
- ✅ Nenhuma ocorrência de `.orElse(null)` encontrada no código
- Os arquivos *DataServiceJpa.java foram corrigidos

**TD-006: Falta de timeouts e retry**
- ✅ Implementado retry logic com exponential backoff em `AIWebSearchScraperClient`
- ✅ Adicionado timeout usando ExecutorService e Future
- ✅ Configuração via `ScraperProperties` (timeout: 30s, maxRetries: 2)
- ✅ Aplicado em ambos métodos: `scrapeWebsiteSync()` e `searchNews()`

**TD-007: Erro sintático em teste**
- ✅ EnrichmentPipelineIntegrationTest.java compila sem erros
- Todos os testes estão sintaticamente corretos

**TD-008: Duplicação de métodos toDomainCompany**
- ✅ Criada classe utilitária `CompanyMapper` no pacote `dev.prospectos.api.mapper`
- ✅ Método estático `CompanyMapper.toDomain(CompanyDTO)` centraliza a conversão
- ✅ `CompanyScoringService` atualizado para usar o mapper
- ✅ `ProspectingWorkflowIntegrationTest` atualizado para usar o mapper
- ✅ Imports desnecessários removidos (Website)

### ❌ Itens Pendentes

**TD-001: SNAPSHOT e Milestone dependencies**
- ❌ `build.gradle:3` - Spring Boot `3.5.10-SNAPSHOT`
- ❌ `build.gradle:26` - Spring AI `1.0.0-M4`
- ❌ Repositórios snapshot e milestone ainda configurados (linhas 20-21)

**TD-002: Arquivo .env commitado**
- ❌ Arquivo `.env` ainda existe no repositório (3371 bytes)
- ❌ Arquivo aparece como modificado no git status
- ⚠️ **CRÍTICO**: Pode conter secrets que precisam ser rotacionados


## Pontos Positivos Identificados

✅ **Arquitetura sólida**: Spring Modulith boundaries bem respeitados
✅ **Domain Design**: Value Objects (Email, Website) bem implementados
✅ **Segurança**: DotenvEnvironmentPostProcessor e .gitignore configurados adequadamente
✅ **Testes**: Boa cobertura com testes de integração e boundary tests

## Próximos Passos

1. **TD-002 (CRITICAL)**: Remover .env do repositório
   - Remover arquivo do repositório com `git rm .env`
   - Limpar histórico do git (opcional mas recomendado)
   - Rotacionar qualquer API key que tenha sido commitada
   - Atualizar documentação com instruções de como configurar .env localmente

2. **TD-001 (CRITICAL)**: Atualizar para versões estáveis
   - Verificar disponibilidade de Spring Boot 3.5.x stable
   - Verificar disponibilidade de Spring AI 1.0.0 GA
   - Testar compatibilidade antes de atualizar
   - Remover repositórios snapshot/milestone após migração

3. **Setup CI/CD**: Implementar validações para prevenir regressões
4. **Monitoring**: Configurar observabilidade adequada para AI services

## Progresso

- **Análise inicial**: 2025-01-11
- **Última atualização**: 2026-01-11 (correções implementadas)
- **Método**: Varredura sistemática de código, configurações, testes e build
- **Foco**: Violations de boundaries, práticas de segurança, confiabilidade e maintainability
- **Itens resolvidos**: 6 de 8 (75%)
- **Correções implementadas em 2026-01-11**:
  - TD-003: Removida dependência de .env em testes (11 arquivos atualizados)
  - TD-006: Adicionado timeout e retry em AIWebSearchScraperClient
  - TD-008: Criada classe CompanyMapper para eliminar duplicação