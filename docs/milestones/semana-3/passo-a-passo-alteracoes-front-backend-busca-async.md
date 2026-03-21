# Passo a Passo - Alteracoes Frontend e Backend (Busca Async)

## Fase 0 - Preparacao

### Backend
- [ ] Definir DTO de resposta inicial async (`requestId`, `status`, `message`).
- [ ] Definir DTO de status do job (`status`, `progress`, `leads`, `message`).
- [ ] Definir eventos de dominio da busca async.

### Frontend
- [ ] Definir tipos Zod/TypeScript para:
  - iniciar busca async
  - consultar status por `requestId`
- [ ] Definir estado de tela para `idle`, `processing`, `completed`, `failed`.

---

## Fase 1 - Endpoints novos sem quebrar fluxo atual

### Backend
- [ ] Criar `POST /api/leads/search/async`.
- [ ] Criar `GET /api/leads/search/{requestId}`.
- [ ] Criar `GET /api/leads/search/{requestId}/events` (SSE).
- [ ] Manter `POST /api/leads/search` intacto.
- [ ] Persistir job inicial com status `PROCESSING`.

### Frontend
- [ ] Criar service `startLeadSearchAsync`.
- [ ] Criar service `getLeadSearchStatus`.
- [ ] Criar service para abrir stream SSE por `requestId`.
- [ ] Integrar pagina de busca com opcao de fluxo async (feature flag simples).

---

## Fase 2 - Orquestracao assincrona por eventos

### Backend
- [ ] Criar interface `LeadSearchEventBus` (porta).
- [ ] Criar implementacao `SpringEventLeadSearchEventBus`.
- [ ] Publicar `LeadSearchRequested` ao iniciar job.
- [ ] Criar listener/orquestrador para executar fontes em paralelo.
- [ ] Salvar resultado/falha por fonte com progresso incremental.

### Frontend
- [ ] Iniciar SSE apos receber `requestId`.
- [ ] Exibir barra/texto de progresso (`done/total`).
- [ ] Mostrar falhas parciais sem perder resultados validos.

---

## Fase 3 - Consolidacao e experiencia final

### Backend
- [ ] Consolidar resultados (dedupe + ranking + limit).
- [ ] Marcar job como `COMPLETED` ou `FAILED`.
- [ ] Expor mensagem final consistente para UI.
- [ ] Adicionar TTL e limpeza de jobs antigos.

### Frontend
- [ ] Encerrar stream SSE automaticamente em `COMPLETED`/`FAILED`.
- [ ] Atualizar tabela com resultado final consolidado.
- [ ] Tratar timeout de stream/reconexao com mensagem amigavel.

---

## Fase 4 - Testes (TDD obrigatorio)

### Backend
- [ ] Teste: cria job async e retorna `PROCESSING`.
- [ ] Teste: fonte com erro parcial nao invalida job inteiro.
- [ ] Teste: timeout de fonte marca falha de fonte corretamente.
- [ ] Teste: consolidacao final respeita dedupe/ranking.

### Frontend
- [ ] Teste: inicia busca async e inicia stream SSE.
- [ ] Teste: inicia stream SSE e recebe snapshots.
- [ ] Teste: recebe progresso e atualiza UI.
- [ ] Teste: finaliza em `COMPLETED` e mostra leads.
- [ ] Teste: finaliza em `FAILED` e mostra erro.

---

## Ordem recomendada de implementacao
1. Backend Fase 1 (novos endpoints + status inicial).
2. Frontend Fase 1 (services async + contrato tipado).
3. Backend Fase 2 e 3 (eventos + consolidacao).
4. Frontend Fase 2 e 3 (SSE + UX final).
5. Testes completos (fase 4) e ajuste fino de performance.

## Riscos e mitigacoes
- Risco: queda de conexao SSE.
  - Mitigacao: reconexao controlada com mensagem explicita para o usuario.
- Risco: sobrecarga por fontes paralelas.
  - Mitigacao: limite de concorrencia configuravel.
- Risco: duplicidade de processamento.
  - Mitigacao: idempotencia por `requestId + source`.
