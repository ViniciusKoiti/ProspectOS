# Agentes de Workspace

Este diretório define o contrato operacional dos agentes usados no fluxo de desenvolvimento.

## Arquivos
- `agent-orchestrator.toml`: coordenação, priorização, handoff e critérios de aceite.
- `agent-developer.toml`: único agente autorizado a alterar código e testes.
- `agent-tester.toml`: validação e evidências de teste (sem alterar código).
- `agent-reviewer.toml`: revisão por severidade e decisão de merge (sem alterar código).
- `runtime/session.toml`: estado da sessão ativa (handoffs, status e próximos passos).

## Regra de Permissão (Crítica)
- Somente `developer` pode alterar arquivos de aplicação (`src/**`, `apps/**`).
- `orchestrator`, `tester` e `reviewer` ficam restritos a documentação e artefatos de runtime.

## Fluxo de Comunicação Obrigatório
- `orchestrator -> developer`
- `developer -> tester`
- `tester -> reviewer`
- `reviewer -> orchestrator`

Cada handoff deve incluir:
- `context`
- `requested_action`
- `evidence`
- `status`

## Gate de Testes (Hotfix Atual)
- `required_to_start = false`: execução pode iniciar sem bloquear no teste.
- `required_before_commit = true`: commit exige evidência de teste.
- `required_before_handoff = true`: handoff técnico exige teste.
- `required_before_merge = true`: merge exige validação do tester e decisão do reviewer.

## Como Atualizar Workspaces
1. Aplicar o commit da documentação nas branches de workspace (ex.: `cherry-pick`).
2. Executar `push` de cada branch de workspace para `origin`.
3. Confirmar `tracking` remoto e registrar no `runtime/session.toml`.

## Observação
Este fluxo é complementar ao `AGENTS.md` raiz e foi criado para manter separação clara de responsabilidades entre agentes durante desenvolvimento paralelo.

