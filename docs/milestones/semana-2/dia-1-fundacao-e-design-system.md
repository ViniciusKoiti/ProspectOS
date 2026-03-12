# Semana 2 - Dia 1 - Fundacao e Design System

## Objetivo
Criar a base tecnica do frontend e definir o catalogo inicial de componentes reutilizaveis.

## Entregaveis do dia
- App React + TypeScript criada em `apps/prospectos-web`.
- Tailwind configurado.
- React Router + React Query configurados.
- Estrutura de pastas por responsabilidade.
- Catalogo inicial de componentes base aprovado.

## Passo a passo
1. Criar projeto Vite React TS em `apps/prospectos-web`.
2. Instalar dependencias base (`react-router-dom`, `@tanstack/react-query`, `axios`, `react-hook-form`, `zod`, `@heroicons/react`).
3. Configurar Tailwind e arquivo `src/index.css` com estilos utilitarios de base.
4. Criar arquitetura inicial:
   - `src/components/ui`
   - `src/components/layout`
   - `src/components/features`
   - `src/pages`
   - `src/services`
   - `src/types`
5. Criar `App.tsx` com rotas placeholder:
   - `/`
   - `/search`
   - `/icps`
   - `/companies`
6. Criar `services/api.ts` com axios e tratamento de erro basico.
7. Definir tokens visuais basicos (cores, spacing, tipografia) para consistencia.

## Planejamento de componentes (base reutilizavel)
- `Button`: variantes (`primary`, `secondary`, `ghost`), estado `loading`, estado `disabled`.
- `Input`: label, hint, mensagem de erro, tamanhos padrao.
- `TextArea`: contador opcional, erro visual padrao.
- `Select`: placeholder, opcao vazia, suporte a erro.
- `CheckboxGroup`: para selecao de fontes.
- `Badge`: status simples (`success`, `warning`, `neutral`).
- `Card`: container padrao para blocos de tela.
- `Modal`: base para formularios CRUD (ICP no Dia 4).
- `Table`: cabecalho fixo + linha clicavel + estado vazio.
- `PageHeader`: titulo, subtitulo e area de acao.
- `LoadingState`: skeleton padrao.
- `ErrorState`: bloco de erro padrao com CTA.

## Regras de qualidade para componentes
- Props fortemente tipadas.
- Componente sem logica de negocio acoplada.
- Estados visuais consistentes (hover/focus/disabled).
- Reutilizacao antes de criar novo componente.
- Testes minimos de renderizacao para componentes base criticos.

## Validacoes
- `npm run dev` sem erro.
- `npm run build` sem erro.
- `npx tsc --noEmit` sem erro.
- Rotas basicas navegaveis.

## Definicao de pronto
Fundacao pronta para construir telas reais sem retrabalho estrutural.
