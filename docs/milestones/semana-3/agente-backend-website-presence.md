# AGENTE BACKEND - WEBSITE PRESENCE (SEMANA 3)

**Missao:** ajustar o backend para tratar corretamente empresas com e sem website e priorizar oportunidades comerciais.
**Status geral:** em andamento (P0 backend website presence concluido; pendente rodada completa com Docker/Testcontainers)

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
- [x] Teste de serializacao no endpoint `/api/leads/search` validando explicitamente `websitePresence`.
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
- Commit final com convencao Conventional Commits.

---

## QUADRO DE STATUS RAPIDO

- Status atual: em andamento (P0 backend website presence concluido; validacao targeted sem Docker executada com sucesso).
- Proximo passo: executar rodada de regressao de integracao completa com Docker/Testcontainers e registrar resultado.
- Bloqueios: ambiente atual sem Docker/Testcontainers para testes `test-pg` (`LeadSearchIntegrationTest`).
- Ultima atualizacao: 2026-03-20 (agente backend website presence).

---

## PROTOCOLO DE ATUALIZACAO

Checklist para atualizar este documento apos cada avanco:

- [ ] Atualizar `Status geral` e `Quadro de Status Rapido` com estado real do momento.
- [ ] Revisar `TDD (OBRIGATORIO)` e marcar apenas o que esta comprovadamente validado.
- [ ] Revisar `Checklist de Entrega` e manter coerencia com testes executados.
- [ ] Preservar e atualizar pendencias ja identificadas, removendo apenas quando houver evidencia.
- [ ] Registrar a mudanca no `Log de Atualizacoes` com data, agente, resumo e evidencia objetiva.
- [ ] Incluir comando(s) executado(s) ou referencia de artefato de teste na coluna `Evidencia`.
- [ ] Nao remover secoes existentes; apenas complementar ou marcar progresso.

---

## LOG DE ATUALIZACOES

| Data | Agente | Mudanca | Evidencia |
| --- | --- | --- | --- |
| 2026-03-20 | backend-documentacao | Adicao das secoes operacionais continuas: Quadro de Status Rapido, Protocolo de Atualizacao e Log de Atualizacoes. | Atualizacao em `docs/milestones/semana-3/agente-backend-website-presence.md` com novas secoes e checklist de rotina. |
| 2026-03-20 | backend-website-presence | Remocao de fallback hardcoded de fontes em fluxos legacy (`DefaultLeadDiscoveryService` e `InMemoryLeadSourceResolver`), com erro explicito quando nao ha fonte efetiva configurada. | Edicao em `src/main/java/dev/prospectos/infrastructure/service/discovery/DefaultLeadDiscoveryService.java` e `src/main/java/dev/prospectos/infrastructure/service/inmemory/InMemoryLeadSourceResolver.java`; validado por `./gradlew --no-daemon test --rerun-tasks --tests 'dev.prospectos.infrastructure.service.discovery.DefaultLeadDiscoveryServiceTest' --tests 'dev.prospectos.infrastructure.service.inmemory.InMemoryLeadSearchServiceTest' -x jacocoTestCoverageVerification -x jacocoTestReport`. |
| 2026-03-20 | backend-website-presence | Adicao de teste de serializacao do endpoint `/api/leads/search` validando explicitamente `websitePresence` no payload. | Novo teste `src/test/java/dev/prospectos/infrastructure/api/leads/LeadSearchControllerTest.java`; validado por `./gradlew --no-daemon test --rerun-tasks --tests 'dev.prospectos.infrastructure.api.leads.LeadSearchControllerTest' -x jacocoTestCoverageVerification -x jacocoTestReport`. |
| 2026-03-20 | backend-website-presence | Registro de bloqueio de ambiente para regressao `test-pg`. | Tentativa de execucao: `./gradlew test --tests 'dev.prospectos.integration.LeadSearchIntegrationTest' -x jacocoTestCoverageVerification` falhou em `DockerClientProviderStrategy` (Docker/Testcontainers indisponivel no ambiente). |
