# AGENTE OUTREACH - EMAIL AUTOMATION (FASE 2)

**Missao:** transformar leads priorizados em rotina comercial automatizada por email.
**Status geral:** nao iniciado (em preparacao)

**Ownership sugerido:**
- Backend campanhas: `src/main/java/dev/prospectos/**`
- Frontend operacao: `apps/prospectos-web/src/**`

---

## CONFORMIDADE COM AGENTS.MD

- [ ] TDD obrigatorio para motor de cadencia e regras de parada.
- [ ] Respeitar ownership por pasta (backend e frontend separados).
- [ ] Contratos de API tipados e validados (DTO + schema frontend).
- [ ] Frontend sem `any`, com estados `loading/error/empty`.
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
- [ ] Entidade de campanha e passos de cadencia (`D0`, `D+3`, `D+7`).
- [ ] Endpoint para iniciar campanha por filtro.
- [ ] Logs de eventos: `SENT`, `FAILED`, `BOUNCE`, `REPLIED`.
- [ ] Regra de parada automatica ao detectar resposta.

### Frontend
- [ ] Tela simples para iniciar campanha por segmento.
- [ ] Tabela de execucao com status por lead.
- [ ] Indicadores: enviados, falhas, respostas.

---

## TDD (OBRIGATORIO)

### Backend
- [ ] Teste de criacao de campanha.
- [ ] Teste de transicao de estado da cadencia.
- [ ] Teste de parada por resposta.

### Frontend
- [ ] Teste de renderizacao de status.
- [ ] Teste de acao "iniciar campanha".

---

## CHECKLIST DE ENTREGA

- [ ] Campanha pode ser disparada para segmento "Sem site".
- [ ] Historico completo por lead disponivel para auditoria.
- [ ] Taxas basicas visiveis no frontend.
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

### Pendente
- Escolha e configuracao do provider de envio.
- Implementacao de entidades/endpoints de campanha e cadencia no backend.
- Implementacao de tela operacional e metricas no frontend.
- Cobertura de testes de campanha (backend e frontend).
