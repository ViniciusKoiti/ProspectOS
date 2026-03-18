# AGENTE BACKEND - WEBSITE PRESENCE (SEMANA 3)

**Missao:** ajustar o backend para tratar corretamente empresas com e sem website e priorizar oportunidades comerciais.
**Status geral:** em andamento (implementacao principal concluida, com pendencias de fechamento)

**Ownership exclusivo:**
- `src/main/java/dev/prospectos/**`
- `src/test/java/dev/prospectos/**`

---

## CONFORMIDADE COM AGENTS.MD

- [x] Respeitar boundaries do modulith (`core` sem dependencia de `infrastructure`/`ai`, `api` estavel para contratos e DTOs, `infrastructure` para controllers/adapters).
- [x] TDD por incremento pequeno com testes novos em discovery/search.
- [x] Preferencia por testes unitarios primeiro, integracao apenas quando necessario.
- [ ] Convencao de commit: Conventional Commits (`feat`, `fix`, `test`, `docs`).
- [x] Nao editar arquivos gerados em `build/`.

---

## OBJETIVOS TECNICOS

1. Introduzir campo explicito de presenca de website no payload de leads.
2. Evitar descarte silencioso de candidatos sem website no discovery.
3. Implementar ranking orientado a venda de servicos web.
4. Preservar compatibilidade do fluxo atual de aceite de lead.

---

## ESCOPO (V1)

### Contrato
- [x] Definir `websitePresence` no candidato de lead.
- [x] Estados recomendados: `HAS_WEBSITE`, `NO_WEBSITE`, `UNKNOWN`.

### Pipeline discovery
- [x] Nao descartar automaticamente candidato sem website.
- [x] Introduzir estrategia de `leadKey` fallback para casos sem dominio.
- [x] Manter deduplicacao deterministica.

### Ranking
- [x] Prioridade comercial: `NO_WEBSITE` > `HAS_WEBSITE`.
- [x] Desempate por disponibilidade de contato.
- [x] Limite aplicado apos ranking consolidado.

---

## TDD (OBRIGATORIO)

### Ciclo 1 - Contrato
- [ ] Teste de serializacao no endpoint `/api/leads/search` validando explicitamente `websitePresence`.
- [x] Implementacao minima para contrato e mapeamento do campo.

### Ciclo 2 - Discovery sem website
- [x] Teste cobrindo candidato sem website que antes poderia ser descartado.
- [x] Implementacao para manter o candidato no preview.

### Ciclo 3 - Ranking
- [x] Teste validando ordem por oportunidade.
- [x] Implementacao do comparador/ranking.

### Ciclo 4 - Regressao
- [ ] Testes de integracao adicionais para garantir comportamento antigo em leads com website.

---

## CHECKLIST DE ENTREGA

- [x] `websitePresence` presente no retorno de `/api/leads/search`.
- [x] Lead sem website aparece no preview com status correto.
- [x] Ranking comercial aplicado antes do `limit`.
- [x] Cobertura de testes atualizada nos arquivos alterados.
- [ ] Nenhuma regressao em testes existentes de discovery/search (pendente rodada final completa).

---

## COMANDOS DE VALIDACAO

```bash
$env:GRADLE_USER_HOME='D:\Cursos\prospectos\.gradle-user-home'
./gradlew test --tests 'dev.prospectos.infrastructure.service.leads.*'
./gradlew test --tests 'dev.prospectos.infrastructure.service.discovery.*'
./gradlew test
```

---

## CRITERIOS DE ACEITE

- [x] API entrega informacao suficiente para o frontend filtrar "Sem site".
- [x] Regras de ordenacao sao previsiveis e testadas.
- [x] Sem mudanca destrutiva no contrato antigo que quebre a tela atual.

---

## RESUMO DO STATUS (REALIZADO X PENDENTE)

### Realizado
- Campo `websitePresence` incorporado no contrato e mapeamentos.
- Discovery preserva candidatos sem website e gera `leadKey` estavel para esse caso.
- Ranking comercial implementado com prioridade para `NO_WEBSITE`.
- Refatoracao de classes para limite de tamanho e separacao de responsabilidades.

### Pendente
- Rodada de regressao de integracao completa em ambiente com Docker/Testcontainers disponivel.
- Remover defaults hardcoded restantes de fontes em fluxos legacy (`DefaultLeadDiscoveryService` e `InMemoryLeadSourceResolver`) para ficar 100% orientado a configuracao.
- Commit final com convencao Conventional Commits.
