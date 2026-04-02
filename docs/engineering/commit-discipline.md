# Commit Discipline

## Objetivo
Manter o histórico do repositório pequeno, legível e compatível com commitlint.

## Regra principal
Sempre que houver um pequeno ganho funcional, estrutural ou de segurança com testes verdes, gere um commit.

## O que conta como pequeno ganho
- uma correção de bug fechada de ponta a ponta
- uma refatoração segura protegida por testes
- um ajuste de infraestrutura/configuração com validação local
- uma melhoria de cobertura que fecha um gate do repositório
- uma documentação operacional diretamente ligada ao comportamento entregue

## O que não deve virar commit misturado
- arquivo local de ferramenta ou IDE
- rascunho de prompt
- notas temporárias de investigação
- mudanças sem relação direta com o ganho principal

## Formato obrigatório
Use Conventional Commits:

```text
<type>(<scope>): <resumo curto>
```

Exemplos válidos:
- `feat(mcp): add provider routing tools`
- `fix(mcp): isolate server beans behind mcp profile`
- `refactor(mcp): split large query history services`
- `test(mcp): add coverage for security filters`
- `docs(process): add commit discipline guide`

## Tipos aceitos
- `build`
- `chore`
- `ci`
- `docs`
- `feat`
- `fix`
- `perf`
- `refactor`
- `revert`
- `style`
- `test`

## Sequência recomendada antes de commitar
1. Confirmar que o ganho está fechado.
2. Rodar os testes mínimos do escopo alterado.
3. Revisar o `git diff --staged`.
4. Garantir que o commit contém uma única intenção principal.
5. Criar o commit com mensagem commitlint-friendly.

## Heurística prática
Se a descrição do commit precisar de "e também", provavelmente ele está grande demais.

## Regra para este repositório
- Prefira commits por slice vertical.
- Quando houver mistura entre código de produto, refatoração e documentação, separe.
- Quando um gate falhar por causa de cobertura ou class length, corrija isso no mesmo commit do comportamento afetado ou em um commit técnico imediatamente seguinte.
