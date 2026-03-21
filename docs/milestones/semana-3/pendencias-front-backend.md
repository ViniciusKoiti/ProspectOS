# Semana 3 - Pendencias Frontend e Backend

## Status atual
- Escopo de `websitePresence` concluido em frontend (P0/P1) e backend (P0).
- Escopo inicial de outreach email entregue (frontend + backend + testes direcionados).
- Regressao de integracao backend com Docker/Testcontainers validada com sucesso em 21/03/2026.
- Pendencias abertas concentram em provider real e persistencia de cadencia.

## Frontend

### P0
- [x] Implementar estado vazio especifico quando filtro `Sem site` nao retornar resultados.
  - Criterio de aceite: mensagem contextual exibida sem impactar o estado vazio padrao.
- [x] Entregar tela operacional de outreach por segmento (`/outreach`).
  - Criterio de aceite: iniciar campanha com `segment` + `limit` e exibir status por lead.

### P1
- [x] Revisar `SearchMatchInsights` para incluir visao por `websitePresence` (HAS/NO/UNKNOWN).
  - Criterio de aceite: insight mostra distribuicao coerente com a tabela filtrada.
- [x] Garantir cobertura de teste do estado vazio especifico e dos novos insights.
  - Criterio de aceite: testes de pagina/componentes cobrindo os cenarios novos.
- [x] Cobrir contrato de outreach e fluxo da pagina de outreach com testes.
  - Criterio de aceite: testes de service e pagina validando envio de payload e renderizacao de resultados.

### P2
- [ ] Adicionar UX de historico de campanhas de outreach (lista de execucoes e detalhe por campanha).
  - Criterio de aceite: operacao consegue revisar campanhas anteriores sem rerodar.

## Backend

### P0
- [x] Remover defaults hardcoded restantes de fontes em fluxos legacy:
  - `DefaultLeadDiscoveryService`
  - `InMemoryLeadSourceResolver`
  - Criterio de aceite: fontes efetivas derivadas de configuracao (`prospectos.leads.*`), sem fallback fixo oculto.

- [x] Adicionar teste de serializacao no endpoint `/api/leads/search` validando `websitePresence`.
  - Criterio de aceite: teste de integracao falha sem o campo e passa com o contrato atual.
- [x] Entregar endpoint de campanha de outreach por segmento.
  - Criterio de aceite: `POST /api/outreach/campaigns` aceita `segment` e `limit` com validacao.
- [x] Entregar implementacao inicial em memoria para fluxo de campanha.
  - Criterio de aceite: retorno com `leads`, `sent`, `failures`, `responses`.

### P1
- [ ] Implementar persistencia de campanha/cadencia e historico por lead (auditoria).
  - Criterio de aceite: cada envio/resposta fica rastreavel e consultavel por API.
- [ ] Integrar provider de email e mapear eventos reais (`SENT`, `FAILED`, `BOUNCE`, `REPLIED`).
  - Criterio de aceite: status deixa de ser simulado e passa a refletir retorno do provider.
- [ ] Implementar regra de parada/reenvio baseada em resposta real.
  - Criterio de aceite: leads com resposta nao entram em novos disparos automaticos.
- [x] Rodar regressao de integracao completa em ambiente com Docker/Testcontainers disponivel.
  - Criterio de aceite: suites de discovery/search sem regressao.

## Fechamento da entrega
- [x] Validar testes frontend (`pnpm run test`, `pnpm run build`).
- [x] Validar testes backend focados (sem gate global de coverage).
- [x] Validar regressao final backend com ambiente Docker/Testcontainers disponivel.
- [x] Atualizar checklist dos agentes apos cada fechamento.

## Evidencias recentes
- 21/03/2026: regressao backend com Docker/Testcontainers executada com `./gradlew test --tests '*IntegrationTest' -x jacocoTestReport -x jacocoTestCoverageVerification`.
- Resultado consolidado: `TOTAL_TESTS=98`, `FAILED_OR_ERRORS=0`, `SKIPPED=0`.
