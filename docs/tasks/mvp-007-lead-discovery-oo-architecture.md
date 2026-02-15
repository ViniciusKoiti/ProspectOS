# MVP-007 Lead Discovery com Arquitetura Orientada a Objetos

## Objetivo
Criar um endpoint de descoberta de leads por texto livre (buyers/suppliers), reutilizando o fluxo atual de LLM/scoring e preparando extensão para novas fontes sem refatorações grandes.

## Princípios de design (OO)
- `Open/Closed`: adicionar novas fontes sem alterar o orquestrador.
- `Strategy`: cada fonte implementa a própria estratégia de descoberta.
- `Template Method`: pipeline comum (validar -> descobrir -> normalizar -> score -> responder).
- `Factory/Registry`: seleção dinâmica de estratégias por `source`.
- `Single Responsibility`: separação entre orquestração, descoberta, conversão, scoring e compliance.

## API proposta
- `POST /api/leads/discover`

Request inicial:
```json
{
  "query": "fornecedores de alimentos no interior do paraná",
  "role": "SUPPLIER",
  "limit": 20,
  "sources": ["llm-discovery"],
  "icpId": 1
}
```

## Arquitetura proposta

### 1) Contratos centrais
- `LeadDiscoveryService` (api): contrato de entrada do caso de uso.
- `LeadDiscoverySource` (infra): strategy por fonte.
- `LeadDiscoveryPipeline` (infra): template method do fluxo comum.

### 2) Orquestrador
- `DefaultLeadDiscoveryService implements LeadDiscoveryService`
  - resolve `icpId`
  - valida `sources` via compliance
  - seleciona strategies no registry
  - agrega candidatos e deduplica
  - delega pipeline comum de scoring/resposta

### 3) Strategies por source
- `LlmDiscoverySource implements LeadDiscoverySource` (fase 1)
  - usa cliente LLM já existente
  - reaproveita conversores/sanitizers atuais
  - retorna `DiscoveredLeadCandidate`
- Futuras:
  - `LinkedInDiscoverySource`
  - `ApolloDiscoverySource`
  - `InMemoryDiscoverySource`

### 4) Modelo interno (objetos de domínio de aplicação)
- `LeadDiscoveryRequest` / `LeadDiscoveryResponse`
- `DiscoveredLeadCandidate` (name, website, industry, location, description, contacts, sourceMeta)
- `DiscoveryContext` (query, role, icp, limit, allowedSources)

### 5) Reuso do fluxo atual
- Scoring: `CompanyScoringService` + `ScoringAIService`
- Compliance: `AllowedSourcesComplianceService`
- Proveniência e `leadKey`: reaproveitar convenções do search atual
- Conversão LLM robusta: base já criada (`sanitizer/converter`)

## Template Method (pipeline comum)
1. `validateRequest()`
2. `resolveIcp()`
3. `discoverCandidatesFromSources()`
4. `normalizeAndDeduplicate()`
5. `scoreCandidates()`
6. `buildLeadResponse()`

Passos `1,2,4,5,6` ficam no pipeline base.
Passo `3` varia por strategy.

## Extensibilidade para novos leads/sources
- Novo source = nova classe que implementa `LeadDiscoverySource`.
- Registro automático via Spring (`Map<String, LeadDiscoverySource>`).
- Sem alterar controller/serviço principal.

## Fase 1 (LLM-first, sem integração externa)
- Ativar apenas `llm-discovery`.
- Endpoint funcional com texto livre.
- Resposta com `source.sourceName = "llm-discovery"`.
- Sem dependência de LinkedIn/Apollo.

## Fase 2 (integrações externas)
- Adicionar strategies novas.
- Opcional: agregação por peso de fonte.
- Opcional: fallback hierárquico (se fonte A falhar, tentar B).

## Testes

### Unitários
- `DefaultLeadDiscoveryServiceTest`:
  - resolve ICP
  - valida sources
  - agrega + deduplica
- `LlmDiscoverySourceTest`:
  - parse robusto de payload LLM
  - candidatos mínimos válidos
- `LeadDiscoveryPipelineTest`:
  - pipeline comum e score

### Integração
- `LeadDiscoveryIntegrationTest`:
  - 200 com query textual
  - 400 para source inválida
  - 400 sem ICP e sem default
  - resposta com proveniência e score

## Critérios de aceite
- Descoberta textual funcionando sem integração externa.
- Arquitetura orientada a objetos com strategy + template + registry.
- Inclusão de novas fontes sem tocar no fluxo principal.
- Reuso explícito do scoring e conversão LLM já existentes.

