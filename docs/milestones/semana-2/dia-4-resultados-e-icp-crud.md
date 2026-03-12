# Semana 2 - Dia 4 - Resultados e ICP CRUD

## Objetivo
Exibir resultados de busca com boa usabilidade e entregar CRUD completo de ICP.

## Entregaveis do dia
- Resultado de busca em tabela reutilizavel.
- Acoes por linha (ver detalhe, aceitar lead, etc. quando aplicavel).
- Pagina de ICPs com listar, criar, editar e remover.
- Modal de ICP com validacao.

## Passo a passo
1. Criar `SearchResults` com estados:
   - vazio
   - loading
   - erro
   - sucesso
2. Criar componente de tabela reutilizavel para listas.
3. Implementar pagina `ICPs` com query de listagem.
4. Implementar mutacoes de create/update/delete com invalidacao de cache.
5. Criar modal de formulario de ICP com schema validado.
6. Revisar mensagens de erro/sucesso para operacoes CRUD.

## Planejamento de componentes do dia
- `DataTable`: tabela base com colunas tipadas.
- `SearchResultsTable`: especializacao da tabela para leads.
- `EmptyState`: estado vazio reutilizavel.
- `IcpCard`: resumo de ICP para grid/lista.
- `IcpFormModal`: create e edit no mesmo componente.
- `ConfirmDialog`: confirmacao de delete.
- `Toast`: feedback de sucesso e erro.

## Criterios de qualidade
- Tabela com tipagem forte nas colunas.
- Mutacoes com invalidacao correta (`queryClient.invalidateQueries`).
- Modal reutilizado para criar e editar (sem duplicacao).
- Sem regressao na pagina de busca.

## Validacoes
- Busca renderiza lista corretamente.
- CRUD de ICP funciona ponta a ponta.
- Atualizacao de lista apos mutacao sem refresh manual.
- Sem erro de tipagem no fluxo de tabela e modal.

## Definicao de pronto
Usuario busca prospects e gerencia ICPs com fluxo completo dentro da interface.
