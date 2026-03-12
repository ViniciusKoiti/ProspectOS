# Semana 2 - Dia 2 - Layout e Dashboard

## Objetivo
Construir a estrutura visual principal da aplicacao e a tela inicial com metricas.

## Entregaveis do dia
- Layout global funcional (header + sidebar + conteudo).
- Navegacao entre paginas sem recarregar browser.
- Dashboard com cards de metrica e acoes rapidas.
- Estados de loading e erro no dashboard.

## Passo a passo
1. Criar componentes de layout:
   - `Header`
   - `Sidebar`
   - `MainLayout`
2. Integrar layout no `App.tsx` com rotas.
3. Criar pagina `Dashboard`.
4. Integrar queries basicas para `/companies` e `/icps`.
5. Exibir metricas em cards reutilizaveis.
6. Adicionar fallback para erro e skeleton para loading.

## Planejamento de componentes do dia
- `MainLayout`: estrutura fixa da aplicacao.
- `NavItem`: item de menu com estado ativo/inativo.
- `StatsCard`: titulo, valor, variacao, tipo de variacao.
- `QuickActions`: grupo de botoes de acao.
- `SectionCard`: container para blocos do dashboard.

## Criterios de qualidade
- Layout responsivo: desktop e tablet funcionais.
- Nenhum link usando `a href` para rotas internas (usar `Link`).
- Componentes de layout sem dependencia de endpoints.
- Evitar duplicacao de estilos (usar componentes UI base).

## Validacoes
- Navegacao entre `/`, `/search`, `/icps`, `/companies`.
- Dashboard carrega com dados reais ou fallback.
- Sem erro de console em navegacao e renderizacao.

## Definicao de pronto
Usuario consegue acessar o produto no browser e navegar entre as areas principais.
