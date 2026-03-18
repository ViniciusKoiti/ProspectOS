# AGENTE FRONTEND - WEBSITE PRESENCE (SEMANA 3)

**Missao:** tornar visivel e operavel no frontend a oportunidade "empresa sem site".
**Status geral:** em andamento (alto progresso)

**Ownership exclusivo:**
- `apps/prospectos-web/src/**`

---

## CONFORMIDADE COM AGENTS.MD

- [x] Todo acesso remoto via services tipados (sem `fetch/axios` direto em UI).
- [x] Estado remoto com React Query (`useQuery`/`useMutation`).
- [x] Formularios com `react-hook-form` + Zod.
- [x] Implementar estados `loading`, `error`, `empty`, `disabled`.
- [x] Evitar `any` em props, contratos e schemas.
- [x] Navegacao interna via `react-router-dom` (`Link`, `useNavigate`).

---

## OBJETIVOS TECNICOS

1. Consumir novo campo de presenca de website vindo do backend.
2. Exibir badge claro em tabela/listagens.
3. Criar filtro simples `Todos | Com site | Sem site`.
4. Garantir cobertura de testes para fluxo principal.

---

## ESCOPO (V1)

### Tipos e contratos
- [x] Atualizar schema Zod de leads para incluir `websitePresence`.
- [x] Atualizar tipos TypeScript derivados.

### UI
- [x] Badge visual por status na tabela de resultados.
- [x] Filtro na pagina de busca para presenca de website.
- [ ] Estado vazio especifico para "nenhum lead sem site encontrado".

### Comportamento
- [x] Manter experiencia atual para outros filtros.
- [x] Evitar loading global desnecessario em alteracoes simples de filtro local.

---

## TDD (OBRIGATORIO)

### Ciclo 1 - Contrato
- [x] Teste de parse de `leadContracts` cobrindo novo campo e compatibilidade.
- [x] Ajuste minimo para passar.

### Ciclo 2 - Renderizacao
- [x] Teste para badge "Sem site".
- [x] Implementacao do badge e mapeamento de labels.

### Ciclo 3 - Filtro
- [x] Teste para filtro `Sem site`.
- [x] Implementacao do filtro e verificacao de resultado.

---

## CHECKLIST DE ENTREGA

- [x] `SearchPage` renderiza status de website.
- [x] Filtro `Sem site` funciona sem quebrar filtros existentes.
- [x] Testes de componentes e servicos atualizados.
- [x] Sem uso de `any` em novos contratos.

---

## COMANDOS DE VALIDACAO

```bash
cd apps/prospectos-web
pnpm run test
pnpm run build
```

---

## CRITERIOS DE ACEITE

- [x] Usuario identifica em segundos quais leads nao possuem site.
- [x] Fluxo continua responsivo e sem regressao visual.
- [x] Contrato frontend-backend alinhado e validado por teste.

---

## RESUMO DO STATUS (REALIZADO X PENDENTE)

### Realizado
- Contrato de `websitePresence` com fallback para payload legado no frontend.
- Badge e coluna de presenca de website na tela de busca.
- Filtro `Todos | Com site | Sem site` com processamento local.
- Cobertura de testes em tipos, utilitarios e pagina de busca.

### Pendente
- Estado vazio especifico para o caso de filtro `Sem site` sem resultados.
