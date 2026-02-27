# WS4 - Documentacao e Backlog

## 1) Principais divergencias docs x codigo (priorizadas)

### P0 - Critica
- Variavel de ambiente Groq inconsistente:
  - Codigo: `ChatClientConfig` orienta erro com `GROQ_API_KEY`.
  - Dotenv e `.env.example`: usam `PROSPECTOS_AI_GROQ_API_KEY` (mapeada para `prospectos.ai.groq.api-key`).
  - Risco: onboarding quebrado e configuracao incorreta de provider.
  - Evidencias:
    - `src/main/java/dev/prospectos/ai/config/ChatClientConfig.java`
    - `src/main/java/dev/prospectos/config/DotenvEnvironmentPostProcessor.java`
    - `.env.example`

### P1 - Alta
- Documento tecnico desatualizado sobre `allowed-sources` em teste:
  - Doc (`TD-003-fix-details`): `prospectos.leads.allowed-sources=in-memory`.
  - Codigo atual (`application-test.properties`): `in-memory,vector-company`.
  - Risco: troubleshooting e reproducao de testes com premissas erradas.
  - Evidencias:
    - `docs/technical-debt/TD-003-fix-details.md`
    - `src/test/resources/application-test.properties`

### P2 - Media
- Backlog de tasks nao cobre consolidacao de docs operacionais atuais:
  - Existe task index MVP, mas sem item explicito para reconciliar docs de configuracao/profiles/env vars com estado atual.
  - Risco: divergencia recorrente apos cada hardening tecnico.
  - Evidencias:
    - `docs/tasks/index.md`
    - `README.md`
    - `src/main/resources/application.properties`

## 2) Plano incremental em fases

### Fase 1 - Alinhamento critico (P0)
- Corrigir mensagens e referencias de chave Groq para padrao unico.
- Definir regra oficial: `PROSPECTOS_AI_GROQ_API_KEY` como chave preferencial; `GROQ_API_KEY` apenas se houver compatibilidade explicita (documentada).
- Revisar README e docs tecnicos relacionados a env vars.

### Fase 2 - Sincronizacao de configuracoes (P1)
- Atualizar docs tecnicos de teste com `allowed-sources` e defaults reais.
- Validar consistencia entre:
  - `application.properties`
  - `application-mock.properties`
  - `application-development.properties`
  - `application-test.properties`

### Fase 3 - Hygiene de backlog e governanca (P2)
- Criar/atualizar task dedicada de "doc drift control" no `docs/tasks`.
- Definir checklist minimo obrigatorio em PR para mudancas de configuracao:
  - profile
  - env var
  - propriedade default
  - comportamento de teste

## 3) Checklist de execucao

- [ ] Fechar decisao de nomenclatura oficial das chaves Groq.
- [ ] Corrigir ponto P0 em codigo e documentacao.
- [ ] Atualizar `TD-003-fix-details.md` para estado real de `allowed-sources`.
- [ ] Revisar README para refletir todas as chaves/env vars suportadas e preferenciais.
- [ ] Garantir que exemplos de configuracao por profile estejam coerentes com `src/main/resources` e `src/test/resources`.
- [ ] Registrar task permanente de controle de divergencia docs x codigo no backlog.
- [ ] Rodar `./gradlew test` apos ajustes funcionais (quando houver alteracao de codigo).

## 4) Criterios de aceite

- [ ] Nao existe conflito entre docs e codigo sobre chave Groq (nome e uso).
- [ ] Nao existe conflito entre docs e codigo sobre `prospectos.leads.allowed-sources` no profile `test`.
- [ ] README, docs tecnicos e `.env.example` descrevem a mesma estrategia de configuracao.
- [ ] Backlog contem item explicito para prevenir regressao de documentacao.
- [ ] Revisao manual por amostragem confirma coerencia entre docs e arquivos de properties ativos.
