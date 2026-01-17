# TD-010: Lead Search persiste dados durante preview

**Status:** OPEN  
**Priority:** P0 (CRITICAL - Product)  
**Created:** 2025-01-17  
**Module:** `infrastructure.service.leads`

## Problem

`POST /api/leads/search` atualmente persiste dados (Company + SourceProvenance) durante a operação de busca, violando o requisito de negócio: "persistir apenas quando o usuário aceitar o lead".

### Evidence

- `ScraperLeadSearchService.searchLeads()` chama:
  - `companyDataService.createCompany(...)` (linha ~78)
  - `sourceProvenanceService.record(...)` (linha ~93)
- Resultado: banco de dados fica poluído com leads não aceitos pelo usuário

### Business Impact

- Viola requisito fundamental do produto
- Cria registros "órfãos" sem consentimento do usuário
- Degrada qualidade dos dados (Companies sem flag de "aceito")
- Impossibilita implementar fluxo de "aceite explícito"

## Solution

Transformar `POST /api/leads/search` em operação de **preview puro**:
- Retornar candidatos enriquecidos + score + proveniência
- **NÃO** persistir nada (nem Company, nem SourceProvenance, nem Score)
- Deixar persistência exclusivamente para `POST /api/leads/accept`

### Implementation Steps

1. Remover chamadas de persistência do `ScraperLeadSearchService`
2. Remover chamadas de persistência do `InMemoryLeadSearchService`
3. Garantir que nenhum serviço intermediário persiste dados
4. Atualizar testes de integração para validar "não persistência"

### Acceptance Criteria

- [x] `POST /api/leads/search` não chama `companyDataService.createCompany()`
- [x] `POST /api/leads/search` não chama `sourceProvenanceService.record()`
- [x] Response continua contendo todos os dados necessários para aceite posterior
- [x] Testes validam que banco permanece vazio após search

## Dependencies

- TD-011 (endpoint de accept) deve ser implementado em paralelo ou antes
- TD-014 (DTO de candidato) facilita a implementação

## Notes

Este TD é **bloqueador** para o valor do produto. Sem ele, o conceito de "aceite de lead" não faz sentido.
