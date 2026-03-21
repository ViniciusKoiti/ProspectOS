# AGENTE OUTREACH - EMAIL AUTOMATION (FASE 2)

**Missao:** transformar leads priorizados em rotina comercial automatizada por email.
**Status geral:** em andamento (MVP operacional inicial entregue)

**Ownership sugerido:**
- Backend campanhas: `src/main/java/dev/prospectos/**`
- Frontend operacao: `apps/prospectos-web/src/**`

---

## CONFORMIDADE COM AGENTS.MD

- [x] TDD obrigatorio para comportamento inicial de campanha (frontend e backend).
- [x] Respeitar ownership por pasta (backend e frontend separados).
- [x] Contratos de API tipados e validados (DTO + schema frontend).
- [x] Frontend sem `any`, com estados `loading/error/empty`.
- [ ] Commits seguindo Conventional Commits.

---

## OBJETIVOS TECNICOS

1. Criar cadencia de emails para leads qualificados.
2. Registrar historico de envios e respostas por lead.
3. Permitir operacao simples de campanha no frontend.

---

## PRE-REQUISITOS

- [x] Campo de segmentacao pronto no lead (`websitePresence`).
- [x] Regras de priorizacao da fase 1 concluidas.
- [ ] Provider de envio escolhido (Resend, SendGrid ou SES).

---

## ESCOPO (V1)

### Backend
- [ ] Entidade persistente de campanha e passos de cadencia (`D0`, `D+3`, `D+7`).
- [x] Endpoint para iniciar campanha por filtro.
- [ ] Logs de eventos: `SENT`, `FAILED`, `BOUNCE`, `REPLIED`.
- [ ] Regra de parada automatica ao detectar resposta.
- [x] Aplicacao de limite de leads por request (`limit` com validacao 1..500).

### Frontend
- [x] Tela simples para iniciar campanha por segmento.
- [x] Tabela de execucao com status por lead.
- [x] Indicadores: enviados, falhas, respostas.

---

## TDD (OBRIGATORIO)

### Backend
- [x] Teste de criacao de campanha.
- [ ] Teste de transicao de estado da cadencia.
- [ ] Teste de parada por resposta.

### Frontend
- [x] Teste de renderizacao de status.
- [x] Teste de acao "iniciar campanha".

---

## CHECKLIST DE ENTREGA

- [x] Campanha pode ser disparada para segmento "Sem site".
- [ ] Historico completo por lead disponivel para auditoria.
- [x] Taxas basicas visiveis no frontend.
- [ ] Sem reenvio para lead que ja respondeu.

---

## CRITERIOS DE ACEITE

- [ ] Time comercial consegue executar outreach sem processo manual repetitivo.
- [ ] Sistema evita duplicidade e respeita estado da conversa.
- [ ] Base de metricas pronta para otimizar copy e cadencia.

---

## RESUMO DO STATUS (REALIZADO X PENDENTE)

### Realizado
- Segmentacao por `websitePresence` pronta para uso como entrada de campanha.
- Priorizacao de leads voltada para oportunidade comercial de criacao/ajuste de site.
- Contrato backend criado para outreach:
  - `POST /api/outreach/campaigns` com `segment` e `limit`.
  - Resposta com `campaignId`, `leads`, `sent`, `failures`, `responses`.
- Implementacao inicial em memoria para operacao e demo da campanha.
- Tela de outreach adicionada no frontend com:
  - formulario de segmento e limite,
  - tabela de status por lead,
  - indicadores agregados,
  - estados de `loading/error/empty`.
- Testes adicionados:
  - Backend: controller + service (`OutreachCampaignControllerTest`, `InMemoryOutreachCampaignServiceTest`).
  - Frontend: service contract + page flow (`outreachService.test.ts`, `OutreachPage.test.tsx`).

### Pendente
- Escolha e configuracao do provider de envio.
- Persistencia de campanha/cadencia (hoje execucao e in-memory, sem historico auditavel).
- Logs de evento de entrega e resposta (`SENT`, `FAILED`, `BOUNCE`, `REPLIED`).
- Regra de parada/reenvio baseada em historico real de resposta.
- Integracao com provider (Resend/SendGrid/SES) e controle de retries.
- Cobertura de testes para transicao de cadencia e regra de parada.
