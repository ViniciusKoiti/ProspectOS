# Plano - Busca de Leads Assincrona por Eventos

## Objetivo
- Evoluir a busca de leads para multiplas fontes em paralelo.
- Manter o endpoint atual funcionando sem quebra.
- Preparar arquitetura para migracao futura para broker (Rabbit/Kafka).

## Estado atual
- Fluxo principal e sincrono em `/api/leads/search`.
- Fontes sao processadas em loop, no mesmo request HTTP.
- Existe `LeadSearchStatus.PROCESSING`, mas o endpoint atual responde resultado final.

## Proposta de evolucao

### 1. Contrato novo (sem quebrar o atual)
- Manter: `POST /api/leads/search` (sincrono).
- Criar: `POST /api/leads/search/async`.
- Criar: `GET /api/leads/search/{requestId}` para status/polling.
- Opcional futuro: `GET /api/leads/search/{requestId}/events` via SSE.

### 2. Arquitetura por interface (facil migracao)
- `LeadSearchEventBus` (porta em `api`).
- `LeadSearchJobStore` (porta para estado e progresso).
- Implementacao inicial:
  - `SpringEventLeadSearchEventBus` com `ApplicationEventPublisher`.
  - Persistencia em banco para `LeadSearchJob` e `LeadSearchSourceRun`.
- Implementacao futura:
  - trocar somente `LeadSearchEventBus` para Rabbit/Kafka.

### 3. Eventos recomendados
- `LeadSearchRequested`
- `LeadSourceProcessingStarted`
- `LeadSourceCompleted`
- `LeadSourceFailed`
- `LeadSearchCompleted`
- `LeadSearchFailed`

## Fluxo tecnico
1. Front chama `POST /api/leads/search/async`.
2. Backend cria `requestId`, salva job com `PROCESSING` e publica `LeadSearchRequested`.
3. Orquestrador faz fan-out por fonte (pool com limite de concorrencia).
4. Cada fonte publica sucesso/falha.
5. Agregador atualiza progresso e consolida (dedupe + ranking + limit).
6. Status final vai para `COMPLETED` ou `FAILED`.
7. Front consulta `GET /api/leads/search/{requestId}` ate finalizar.

## Polling recomendado (baixo custo)
- 0-10s: a cada 2s
- 10-30s: a cada 5s
- >30s: a cada 10s
- parar em `COMPLETED`/`FAILED`
- timeout global no front (ex.: 90s)
- endpoint de status deve retornar payload leve durante `PROCESSING`

## Impacto esperado
- Melhor latencia percebida para o usuario.
- Melhor escalabilidade para adicionar novas fontes.
- Menor risco de timeout de request unico.
- Maior controle de falhas parciais por fonte.

## Riscos e mitigacoes
- Risco: excesso de concorrencia.
  - Mitigacao: limite de workers e timeout por fonte.
- Risco: duplicidade de processamento.
  - Mitigacao: idempotencia por `requestId + source`.
- Risco: custo alto de polling.
  - Mitigacao: backoff + payload leve + futuro SSE.

## Backlog sugerido (TDD)

### Fase 1 - Base async sem frontend novo
- [ ] Criar DTO de inicio async (`requestId`, `status`, `message`).
- [ ] Criar endpoint `POST /api/leads/search/async`.
- [ ] Criar endpoint `GET /api/leads/search/{requestId}`.
- [ ] Criar eventos e interfaces (`LeadSearchEventBus`, `LeadSearchJobStore`).
- [ ] Testes de contrato e validacao de status.

### Fase 2 - Execucao paralela por fonte
- [ ] Orquestrador de fontes com limite de concorrencia.
- [ ] Persistencia de progresso por fonte.
- [ ] Consolidacao final com dedupe/ranking existente.
- [ ] Testes de falha parcial e timeout por fonte.

### Fase 3 - Frontend
- [ ] Service async para iniciar busca e consultar status.
- [ ] Polling com backoff no fluxo de busca.
- [ ] Estados de UI: `processing`, `partial failure`, `completed`, `failed`.
- [ ] Testes de polling e transicao de estados.

### Fase 4 - Evolucao opcional
- [ ] SSE para atualizacao em tempo real.
- [ ] Troca do bus de eventos para Rabbit/Kafka sem quebrar dominio.

## Criterios de aceite
- Busca async responde rapido com `requestId`.
- Progresso por fonte visivel durante `PROCESSING`.
- Falha de uma fonte nao derruba busca inteira.
- Resultado final preserva dedupe/ranking ja adotado.
- Endpoint sincrono atual continua funcional.
