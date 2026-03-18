# MVP SEMANA 3 - PLANO POR AGENTES (WEBSITE PRESENCE)

**Periodo:** 5 dias uteis  
**Objetivo:** priorizar oportunidades para devs que vendem criacao e manutencao de sites  
**Meta:** identificar `tem site` vs `sem site`, manter contatos e habilitar abordagem comercial

---

## OVERVIEW

### Problema atual
- O fluxo favorece fontes com poucos contatos no inicio.
- Leads sem website tendem a ser descartados cedo no pipeline.
- Frontend ainda nao prioriza visualmente oportunidade "sem site".

### Resultado esperado da semana
- Leads com campo explicito de presenca de website.
- Ranking orientado para oportunidade comercial.
- Filtro frontend para `sem site`.
- Base pronta para automacao de outreach por email.

---

## DIVISAO POR AGENTES

### AGENTE 1 - BACKEND (OWNER)
- **Ownership:** `src/main/java/dev/prospectos/**`, `src/test/java/dev/prospectos/**`
- **Foco:** modelo, pipeline de discovery, ranking e contratos de API
- **Dependencias:** nenhuma para iniciar

### AGENTE 2 - FRONTEND
- **Ownership:** `apps/prospectos-web/src/**`
- **Foco:** tipos, filtros, badges, tabela e estados de UX
- **Dependencias:** contrato de API definido pelo Agente 1

### AGENTE 3 - OUTREACH (EMAIL) [FASE 2]
- **Ownership:** backend de campanhas + frontend de operacao
- **Foco:** cadencia de email e historico por lead
- **Dependencias:** conclusao do Agente 1 (campos de segmentacao)

---

## CONFORMIDADE COM AGENTS.MD

- [ ] TDD obrigatorio em todos os agentes (Red -> Green -> Refactor).
- [ ] Backend respeita limites de modulith: `core`, `api`, `ai`, `infrastructure`.
- [ ] Frontend segue contrato React: React Query + react-hook-form + Zod, sem `any`.
- [ ] Alteracoes ficam apenas nas pastas de ownership (sem cruzar backend/frontend sem necessidade).
- [ ] Commits em Conventional Commits (`feat(...)`, `fix(...)`, `test(...)`, `docs(...)`).
- [ ] Nenhuma edicao em `build/` e nenhum segredo em `.env` versionado.

---

## CRONOGRAMA (ALTO NIVEL)

### DIA 1
- [ ] Agente 1 fecha contrato `websitePresence` e testes de contrato.
- [ ] Agente 2 prepara componentes visuais (sem bloquear com mock local).

### DIA 2
- [ ] Agente 1 implementa pipeline para nao perder lead sem website.
- [ ] Agente 2 integra novo campo no schema e na tabela.

### DIA 3
- [ ] Agente 1 implementa ranking por oportunidade comercial.
- [ ] Agente 2 implementa filtro `Com site / Sem site`.

### DIA 4
- [ ] Testes de integracao backend + testes de tela frontend.
- [ ] Ajustes de UX e mensagens de estado vazio.

### DIA 5
- [ ] Hardening, regressao e pacote demo-ready.
- [ ] Preparacao da fase 2 (outreach de email).

---

## DEFINICAO DE PRONTO

- [ ] Endpoint `/api/leads/search` retorna campo de presenca de website.
- [ ] Leads sem website aparecem no preview (nao sao descartados sem explicacao).
- [ ] Frontend permite filtrar e visualizar `Sem site`.
- [ ] Testes backend e frontend cobrindo caminho feliz e casos de borda.
- [ ] Documentacao atualizada com regras de priorizacao.

---

## RISCOS E MITIGACOES

- **Risco:** quebra de contrato entre backend e frontend.  
  **Mitigacao:** congelar contrato no Dia 1 e usar testes de contrato.

- **Risco:** regressao no fluxo de aceite de lead.  
  **Mitigacao:** manter regra explicita para `lead sem website` (desabilitar aceitar na fase 1 ou fallback seguro).

- **Risco:** ranking instavel em producao/demo.  
  **Mitigacao:** regras deterministicas e testes com fixtures fixas.
