# Escopo Necessario - Busca de Leads Async por Eventos

## Objetivo
- Permitir busca de leads em multiplas fontes de forma assincrona.
- Manter o endpoint atual sincrono funcionando para compatibilidade.
- Reduzir tempo de resposta percebido e preparar escalabilidade para novas integracoes.
- Entregar atualizacao em tempo real para a UI via SSE.

## Problema atual
- Busca principal roda no request HTTP e processa fontes no mesmo fluxo.
- Quanto mais fontes, maior risco de lentidao e timeout.
- Falhas de fonte ainda geram acoplamento forte com o fluxo unico.

## O que precisa ser feito

### 1) Contrato de API
- Manter `POST /api/leads/search` sem mudanca de contrato.
- Criar `POST /api/leads/search/async`:
  - entrada igual ao endpoint atual (`query`, `limit`, `sources`, `icpId`)
  - retorno rapido com `requestId`, `status=PROCESSING`, `message`
- Criar `GET /api/leads/search/{requestId}`:
  - retorna progresso, status e resultado final quando concluido
- Criar `GET /api/leads/search/{requestId}/events`:
  - stream SSE com snapshots de progresso e finalizacao

### 2) Modelo de execucao
- Criar um job de busca com `requestId` (correlation id).
- Persistir estado por job:
  - `PROCESSING`, `COMPLETED`, `FAILED`
  - timestamps de inicio/fim
  - progresso (`doneSources/totalSources`)
- Persistir estado por fonte:
  - `sourceName`, `status`, `message`, `durationMs`

### 3) Arquitetura por eventos (migravel)
- Criar porta `LeadSearchEventBus` na camada de contrato.
- Criar implementacao inicial com Spring events (`ApplicationEventPublisher`).
- Criar eventos principais:
  - `LeadSearchRequested`
  - `LeadSourceCompleted`
  - `LeadSourceFailed`
  - `LeadSearchCompleted`
  - `LeadSearchFailed`
- Separar orquestracao de fonte do controller HTTP.

### 4) Paralelismo e resiliencia
- Processar fontes em paralelo com limite de concorrencia configuravel.
- Timeout por fonte.
- Suporte a falha parcial (fonte cai, busca continua).
- Dedupe + ranking final preservando regra atual.

### 5) Observabilidade e operacao
- Logs por `requestId` para rastreio fim a fim.
- Metricas minimas:
  - tempo medio por fonte
  - taxa de falha por fonte
  - tempo total de job
- Politica de limpeza de jobs antigos (TTL).

### 6) Testes obrigatorios
- Backend:
  - criar job async retorna `PROCESSING`
  - finalizacao com sucesso e com falha parcial
  - timeout por fonte
  - dedupe/ranking final sem regressao
- Frontend:
  - fluxo SSE (inicio, progresso, fim)
  - estados `processing`, `completed`, `failed`
  - exibicao de erro parcial por fonte

## Criterios de aceite
- Endpoint sincrono atual continua funcional.
- Endpoint async retorna em baixa latencia com `requestId`.
- Busca multi-fonte conclui sem bloquear request inicial.
- Falha de uma fonte nao derruba resultado das demais.
- Frontend consegue acompanhar progresso e resultado final.

## Fora de escopo (neste primeiro ciclo)
- Substituir Spring events por Rabbit/Kafka.
