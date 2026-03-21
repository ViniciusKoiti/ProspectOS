# Analise de Complexidade - Novas Integracoes (2026-03-21)

## Contexto atual
- Ja existe um fluxo inicial de outreach:
  - Backend: `POST /api/outreach/campaigns` com execucao in-memory.
  - Frontend: tela `/outreach` com formulario, tabela e indicadores.
- Ainda nao existe provider real de envio, historico persistido por lead, webhook de eventos ou regra real de parada/reenvio.

## Escala de complexidade usada
- **Baixa:** ate 2 dias, baixo risco tecnico.
- **Media:** 3 a 6 dias, envolve adaptador + testes + ajustes de contrato.
- **Alta:** 7 a 12 dias, envolve persistencia, eventos, idempotencia e observabilidade.
- **Muito alta:** acima de 12 dias, multiplos provedores/processos distribuidos.

## Matriz de integracoes

### 1) Provider de email (envio basico) - Resend/SendGrid/SES
- Complexidade: **Media**
- Estimativa: **3 a 5 dias**
- Escopo:
  - Criar `EmailDeliveryService` (porta no modulo `api`).
  - Implementar adapter provider no `infrastructure`.
  - Configurar chaves/propriedades por profile.
  - Substituir simulacao de envio por envio real.
- Principais riscos:
  - Rate limit e retry.
  - Erros transientes da API externa.
  - Diferencas de payload entre provedores.

### 2) Webhook de eventos (SENT/FAILED/BOUNCE/REPLIED)
- Complexidade: **Alta**
- Estimativa: **4 a 7 dias** (alem do provider basico)
- Escopo:
  - Endpoint webhook assinado (validacao de assinatura/HMAC).
  - Mapeamento de eventos para estados internos.
  - Idempotencia para eventos duplicados.
  - Atualizacao de status por lead/campanha.
- Principais riscos:
  - Seguranca de webhook.
  - Ordem de eventos fora de sequencia.
  - Retries do provider causando duplicidade.

### 3) Cadencia com regra de parada por resposta
- Complexidade: **Alta**
- Estimativa: **5 a 8 dias** (apos provider + webhook)
- Escopo:
  - Persistencia de campanha e passos (`D0`, `D+3`, `D+7`).
  - Job/scheduler de proximos disparos.
  - Regra "parar se respondeu".
  - Auditoria por lead.
- Principais riscos:
  - Reenvio indevido sem trava de estado.
  - Concorrencia de atualizacao de status.
  - Falta de trilha auditavel.

### 4) Verificador tecnico de website (tem site, quebrado, sem SSL, etc.)
- Complexidade: **Media**
- Estimativa: **3 a 6 dias**
- Escopo:
  - Adaptador HTTP (timeout/retry) para checks tecnicos.
  - Score de saude do site.
  - Campo normalizado para filtro comercial.
- Principais riscos:
  - Falsos positivos por bloqueio/bot protection.
  - Timeouts em massa.
  - Custo de execucao em lotes.

### 5) Integracao CRM (Pipedrive/HubSpot)
- Complexidade: **Alta**
- Estimativa: **6 a 10 dias**
- Escopo:
  - Exportacao de lead/campanha para CRM.
  - Deduplicacao por chave externa.
  - Sincronizacao de status bidirecional (opcional).
- Principais riscos:
  - Conflito de modelo de dados.
  - Duplicidade de lead.
  - Governanca de permissao/API key.

## Recomendacao pragmatica (ordem)
1. Provider de email basico (valor rapido e baixo risco relativo).
2. Webhook de eventos + idempotencia (para status confiavel).
3. Cadencia persistida com regra de parada (fecha o ciclo comercial).
4. Verificador tecnico de website (melhora segmentacao e prioridade).
5. Integracao CRM (quando fluxo interno estiver estavel).

## Esforco total por pacote

### Pacote MVP email operacional
- Provider basico + webhook minimo + status por lead
- Complexidade: **Alta**
- Esforco: **8 a 12 dias**

### Pacote email completo (cadencia + parada + auditoria)
- Inclui persistencia completa e controle de reenvio
- Complexidade: **Muito alta**
- Esforco: **12 a 18 dias**

## Criterios para escolher proxima integracao
1. Impacto comercial imediato (gera valor em demo/producao).
2. Esforco para operar com confianca (observabilidade e suporte).
3. Risco de regressao em fluxo critico de lead.
4. Dependencia de terceiros (API/limites/custos).
5. Cobertura de testes e capacidade de rollback.

## Documento complementar
- Levantamento de planos gratuitos para fontes de leads: `docs/milestones/semana-3/levantamento-integracoes-gratuitas-leads-2026-03-21.md`
