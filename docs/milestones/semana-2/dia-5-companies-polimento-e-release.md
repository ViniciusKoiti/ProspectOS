# Semana 2 - Dia 5 - Companies, Polimento e Release

## Objetivo
Finalizar a experiencia principal com listagem de empresas, robustez visual e prontidao para demo.

## Entregaveis do dia
- Pagina de empresas funcional.
- Filtros basicos de listagem.
- Error boundary e estados de fallback padronizados.
- Revisao final de performance e build.

## Passo a passo
1. Criar pagina `/companies` com consulta e renderizacao em cards/tabela.
2. Implementar filtros basicos (industria, score minimo, localizacao).
3. Criar bloco de detalhe rapido (drawer/modal ou card expandido).
4. Integrar `ErrorBoundary` no nivel de layout/pagina.
5. Revisar carregamento e mensagens de falha em todas as telas.
6. Executar checklist final de build e smoke test manual.

## Planejamento de componentes do dia
- `CompanyCard`: resumo visual de empresa.
- `CompanyFilters`: formulario de filtros reutilizavel.
- `FilterChips`: visualizacao de filtros ativos.
- `CompanyQuickView`: detalhe rapido sem sair da tela.
- `ErrorBoundary`: fallback global.
- `AppSkeleton`: skeleton padrao para paginas com dados.

## Criterios de qualidade
- Componentes com responsabilidades claras e pequenas.
- Props tipadas e sem `any` em componentes novos.
- Acessibilidade basica: labels, foco visivel, botoes semanticos.
- Fluxo principal utilizavel em desktop e mobile.

## Validacoes finais
- `npm run build` sem erro.
- `npx tsc --noEmit` sem erro.
- Teste manual dos fluxos:
  - navegar entre paginas
  - buscar prospects
  - CRUD de ICP
  - listar empresas com filtros
- Sem erros criticos no console.

## Definicao de pronto
Semana 2 concluida com frontend funcional, padronizado em componentes reutilizaveis e pronto para demo.
