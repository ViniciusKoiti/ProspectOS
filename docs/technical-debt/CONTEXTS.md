# Technical Debt Contexts

Data de referencia: 2026-02-18

Este documento organiza os TDs por contexto para facilitar planejamento e execucao.
Status foi validado contra codigo + documentacao de `docs/technical-debt/`.

## Contexto 1 - Seguranca e Secrets (P0)
Objetivo: eliminar risco ativo de exposicao de credenciais.

- TD-002 - Arquivo `.env` commitado no repositorio
  - Status real: OPEN
  - Modulo: cross
  - Risco: critico (security/compliance)
  - Dependencias: nenhuma
  - Resultado esperado:
    - remover `.env` do versionamento
    - rotacionar credenciais expostas
    - reforcar protecoes (pre-commit/CI)

## Contexto 2 - Build e Estabilidade de Plataforma (P1)
Objetivo: tornar build deterministico e suportado para producao.

- TD-001 - Dependencias SNAPSHOT/Milestone
  - Status real: OPEN
  - Modulo: cross
  - Risco: critico (stability/release risk)
  - Dependencias: idealmente apos TD-002 (para reduzir risco operacional durante mudancas)
  - Resultado esperado:
    - migrar Spring Boot/Spring AI para versoes estaveis
    - remover repositorios de milestone/snapshot
    - validar suite de testes completa

## Contexto 3 - Fluxo de Produto Preview -> Accept (P0/P1)
Objetivo: garantir contrato correto do fluxo de leads sem persistencia prematura.

- TD-010 - Preview sem persistencia
- TD-011 - Endpoint de accept
- TD-012 - `icpId` alinhado para `Long`
- TD-013 - Scoring preview desacoplado de persistencia
- TD-014 - `CompanyCandidateDTO` + `leadKey`
- TD-015 - ICP default no lead search
- TD-017 - Mapeamento completo de ICP para scoring

Status real: RESOLVED no codigo (documentacao principal ainda mostra OPEN em alguns pontos).

## Contexto 4 - Hardening Discovery/Scoring (P1)
Objetivo: robustez operacional e performance no discovery.

- TD-018.1 a TD-018.6

Status real: DONE (conforme `TD-018-discovery-and-scoring-hardening.md`).

## Contexto 5 - Qualidade Base (Ja Resolvido)
Objetivo: registrar historico de itens estruturais ja corrigidos.

- TD-003 - testes dependentes de `.env`
- TD-004 - `System.out` em producao
- TD-005 - `Optional.orElse(null)`
- TD-006 - resiliencia em integracoes AI
- TD-007 - erro de compilacao em teste
- TD-008 - duplicacao de mapeamento DTO->Domain

Status real: RESOLVED.

## Ordem Recomendada de Execucao (Somente pendentes)
1. Contexto 1 (TD-002) - risco de seguranca ativo.
2. Contexto 2 (TD-001) - estabilizacao de build e release.

## Checklist de Planejamento por Contexto
Para cada contexto pendente, criar card com:
- escopo tecnico
- riscos/rollback
- testes obrigatorios (unit + integration + modulith quando aplicavel)
- criterio de pronto (DoD)

## Observacoes
- Existe inconsistencia entre status "OPEN" no `docs/technical-debt/README.md` e estado real de parte dos TDs.
- Recomendacao: atualizar o README de debt em uma tarefa dedicada de higiene documental.
