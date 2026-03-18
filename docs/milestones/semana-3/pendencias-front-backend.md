# Semana 3 - Pendencias Frontend e Backend

## Status atual
- Escopo de `websitePresence` avancou em frontend e backend.
- Ainda existem itens de fechamento para concluir a implementacao com seguranca.

## Frontend (pendente)

### P0
- [ ] Implementar estado vazio especifico quando filtro `Sem site` nao retornar resultados.
  - Critrio de aceite: mensagem contextual exibida sem impactar o estado vazio padrao.

### P1
- [ ] Revisar `SearchMatchInsights` para incluir visao por `websitePresence` (HAS/NO/UNKNOWN).
  - Criterio de aceite: insight mostra distribuicao coerente com a tabela filtrada.
- [ ] Garantir cobertura de teste do estado vazio especifico e dos novos insights.
  - Criterio de aceite: testes de pagina/componentes cobrindo os cenarios novos.

## Backend (pendente)

### P0
- [ ] Remover defaults hardcoded restantes de fontes em fluxos legacy:
  - `DefaultLeadDiscoveryService`
  - `InMemoryLeadSourceResolver`
  - Criterio de aceite: fontes efetivas derivadas de configuracao (`prospectos.leads.*`), sem fallback fixo oculto.

- [ ] Adicionar teste de serializacao no endpoint `/api/leads/search` validando `websitePresence`.
  - Criterio de aceite: teste de integracao falha sem o campo e passa com o contrato atual.

### P1
- [ ] Rodar regressao de integracao completa em ambiente com Docker/Testcontainers disponivel.
  - Criterio de aceite: suites de discovery/search sem regressao.

## Fechamento da entrega
- [ ] Validar testes frontend (`pnpm run test`, `pnpm run build`).
- [ ] Validar testes backend focados e regressao final.
- [ ] Atualizar checklist dos agentes apos cada fechamento.
