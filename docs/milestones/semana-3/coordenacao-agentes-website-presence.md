# Coordenacao de Agentes - Website Presence (Semana 3)

## Objetivo
Centralizar o acompanhamento do trabalho em duas frentes (frontend e backend) com atualizacao continua e sem mistura de ownership.

## Separacao de Ownership
- Agente Frontend: `docs/milestones/semana-3/agente-frontend-website-presence.md`
- Agente Backend: `docs/milestones/semana-3/agente-backend-website-presence.md`
- Consolidacao de pendencias: `docs/milestones/semana-3/pendencias-front-backend.md`

## Estado Atual Consolidado
### Frontend
- P0 concluido: estado vazio especifico para filtro `Sem site` sem resultados.
- P1 concluido: `SearchMatchInsights` com distribuicao `HAS/NO/UNKNOWN`.
- P1 concluido: cobertura de testes para estado vazio e insights.

### Backend
- P0 concluido: remover defaults hardcoded de fontes legacy (`DefaultLeadDiscoveryService`, `InMemoryLeadSourceResolver`).
- P0 concluido: teste de serializacao no endpoint `/api/leads/search` para `websitePresence`.
- P1 pendente: rodar regressao completa de integracao com ambiente Docker/Testcontainers.

## Protocolo de Atualizacao (Obrigatorio)
A cada avanco relevante, cada agente deve atualizar seu arquivo proprio com:
- Status atual (`on_track`, `blocked`, `done`).
- Proximo passo imediato.
- Bloqueios tecnicos (se houver).
- Evidencia executavel (comando/teste/arquivo).
- Data da atualizacao.

## Cadencia Recomendada
- Atualizar no minimo ao fechar cada item P0.
- Atualizar ao final de cada rodada de testes.
- Atualizar antes de abrir PR.

## Criterio de Fechamento
Considerar website presence concluido quando:
- Frontend: filtro + estado vazio especifico + testes cobrindo os cenarios.
- Backend: contrato serializado + sem hardcoded legado + regressao validada.
- Documentacao: checklists dos dois agentes atualizados e sem pendencia P0.

## Execucao Atual (2026-03-20)
- Agente FRONTEND concluido: estado vazio `Sem site`, insights por `websitePresence`, testes e atualizacao de doc frontend.
- Agente BACKEND concluido: remocao de defaults hardcoded legacy, teste de serializacao `/api/leads/search` e atualizacao de doc backend.
- Coordenacao principal: validacao final concluida para P0; regressao de integracao com Docker/Testcontainers segue pendente (P1).
