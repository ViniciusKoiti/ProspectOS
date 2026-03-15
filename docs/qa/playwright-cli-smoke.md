# Smoke QA with Playwright CLI

## Goal
Validate core frontend flows quickly before deeper E2E automation.

## Preconditions
- Backend running at `http://localhost:8080`
- Frontend running at `http://localhost:5173`
- Use `localhost` (not `127.0.0.1`) to avoid CORS false negatives.

## Session setup
```powershell
npx --yes --package @playwright/cli playwright-cli -s=prospectos-smoke open http://localhost:5173
npx --yes --package @playwright/cli playwright-cli -s=prospectos-smoke snapshot
```

## Smoke flow
1. Dashboard
- Expected: dashboard cards rendered, no generic error state.

2. Search
- Navigate to `Busca`.
- Fill query.
- Select a valid ICP option (not empty).
- Submit search.
- Expected: no form validation error, no `Falha ao executar a busca`.

3. ICPs
- Navigate to `ICPs`.
- Expected: ICP table loads with rows and action buttons.

4. Companies
- Navigate to `Empresas`.
- Expected: companies table loads and links to detail pages are present.

## Evidence capture
```powershell
npx --yes --package @playwright/cli playwright-cli -s=prospectos-smoke console
npx --yes --package @playwright/cli playwright-cli -s=prospectos-smoke network
npx --yes --package @playwright/cli playwright-cli -s=prospectos-smoke screenshot
```

## Current findings in this session
- `Dashboard`: PASS
- `ICPs`: PASS
- `Companies`: PASS
- `Search`: FAIL (timeout in `/api/leads/search` leading to UI error state)

## Notes
- `favicon.ico 404` appears in console logs and is low severity.
- If search fails, check backend latency and response time for `POST /api/leads/search`.
