# Relatorio de Cobertura - 2026-03-21

## Escopo da analise
- Backend (Spring/Gradle): `./gradlew test jacocoTestReport jacocoTestCoverageVerification`
- Frontend (React/Vitest): `pnpm exec vitest run --coverage --coverage.reporter=json-summary --coverage.reporter=text-summary`

## Resultado consolidado

### Backend (JaCoCo)
- Instruction: `93.79%`
- Branch: `71.64%`
- Line: `92.91%`
- Gate JaCoCo: **PASSOU**

Regras ativas no build:
- Instruction minimo global: `60%`
- Branch minimo global: `50%`
- Instruction minimo por classe: `70%` (com exclusoes)

### Frontend (Vitest Coverage)
- Statements: `77.01%`
- Branches: `69.20%`
- Functions: `73.11%`
- Lines: `77.58%`

## Adequacao atual

### Backend
- Cobertura atual esta **adequada** para o gate definido no projeto.
- Suites de integracao com Docker/Testcontainers tambem executadas sem falhas nessa rodada.

### Frontend
- Cobertura esta **razoavel para MVP**, mas ainda com lacunas em telas/componentes centrais.
- Nao existe gate formal de cobertura no `package.json`/CI (nao ha fail por limiar de cobertura).

## Pontos de atencao importantes

1. Frontend pode estar superestimado
- `App.tsx` e `MainLayout.tsx` nao aparecem no `coverage-summary.json`.
- Isso indica que arquivos nao carregados nos testes nao entram no calculo atual.

2. Backend possui exclusoes no JaCoCo
- Configuracoes e DTOs estao fora do calculo por configuracao do `build.gradle`.
- Isso e intencional, mas precisa ser lembrado ao interpretar os percentuais.

## Hotspots de baixa cobertura (prioridade tecnica)

### Frontend
- `src/components/ui/Modal.tsx`: `27.77%` statements / `18.18%` branches
- `src/components/features/CompanyQuickView.tsx`: `33.33%` / `25%`
- `src/components/features/CompanyFilters.tsx`: `35.71%` / `80%` (funcoes baixas: `18.18%`)
- `src/components/features/FilterChips.tsx`: `40%` / `50%`
- `src/pages/SearchPage.tsx`: `51.94%` / `57.4%`
- `src/pages/CompaniesPage.tsx`: `67.01%` / `68.57%`

### Backend (branch hotspots relevantes)
- `dev.prospectos.ai.client.LlmScoringResponseConverter`: `37.5%` branch
- `dev.prospectos.core.domain.CompanyCoreState`: `37.5%` branch
- `dev.prospectos.infrastructure.service.inmemory.InMemoryLeadResultFactory`: `40%` branch
- `dev.prospectos.infrastructure.service.inmemory.InMemoryCompanyDtoFactory`: `41.67%` branch
- `dev.prospectos.ai.client.LlmScrapingPayloadNormalizer`: `42.31%` branch

## Estado do modulo Outreach (email)
- Backend:
  - `OutreachCampaignController`: `100%` instruction/line
  - `InMemoryOutreachCampaignService`: `96.10%` instruction, `78.57%` branch, `97.14%` line
  - `OutreachCampaignCounters`: `100%`
- Frontend:
  - `OutreachPage.tsx`: `91.66%` statements, `78.57%` branches
  - `outreachService.ts`: `100%`
  - `outreachContracts.ts`: `100%` statements/functions, `85%` branches

Conclusao: para o recorte de outreach entregue agora, a cobertura esta boa.

## Backlog recomendado de cobertura (proximos passos)

### P0 (imediato)
1. Ajustar cobertura do frontend para incluir todo `src/**` no calculo (mesmo arquivo nao importado).
2. Definir gate minimo no frontend CI (sugestao inicial):
   - statements >= `75%`
   - branches >= `65%`
   - functions >= `70%`
3. Cobrir componentes com menor cobertura e alto impacto de UX:
   - `Modal`, `CompanyQuickView`, `CompanyFilters`, `FilterChips`.

### P1 (curto prazo)
1. Subir cobertura de telas criticas:
   - `SearchPage.tsx`
   - `CompaniesPage.tsx`
2. Aumentar branch coverage backend nos factories e parsers mais sensiveis:
   - `InMemoryLeadResultFactory`
   - `InMemoryCompanyDtoFactory`
   - `LlmScrapingPayloadNormalizer`

### P2 (estabilizacao)
1. Testes de contrato frontend-backend para `/api/leads/search` e `/api/outreach/campaigns`.
2. Cobertura de transicoes de cadencia no outreach (parada por resposta/reenvio).

## Observacao final
- O gate backend esta saudavel.
- O principal ganho agora esta no frontend: tornar o calculo de cobertura mais fiel e atacar os hotspots de pagina/componente.
