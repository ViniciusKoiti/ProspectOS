# Semana 2 - Dia 3 - Busca e Formularios

## Objetivo
Entregar a tela de busca de prospects com validacao e integracao com API.

## Entregaveis do dia
- Pagina `/search` funcional.
- Formulario com validacao (`react-hook-form` + `zod`).
- Chamada real para endpoint de busca.
- Tratamento de loading, erro e sucesso.

## Passo a passo
1. Criar schema de validacao da busca.
2. Implementar `SearchForm` com campos:
   - query
   - icpId
   - limit
   - sources
3. Criar hook para submit da busca com React Query (`useMutation`).
4. Integrar lista de ICPs no select do formulario.
5. Conectar submit a API de busca.
6. Mostrar retorno bruto em um painel inicial para validar contrato.

## Planejamento de componentes do dia
- `FormField`: wrapper padrao para label + controle + erro.
- `SearchForm`: formulario completo desacoplado da pagina.
- `IcpSelect`: select dedicado para ICP.
- `SourceSelector`: grupo de checkboxes de fontes.
- `SubmitButton`: botao com estado de loading.
- `SearchRequestPanel` (temporario): visualizacao do payload enviado.

## Criterios de qualidade
- Validacao local clara e mensagens objetivas.
- Sem `any` no formulario e no payload de busca.
- Campos controlados com defaults previsiveis.
- Erro da API exibido de forma amigavel.

## Validacoes
- Submissao bloqueada para query invalida.
- Submissao valida chama endpoint correto.
- Estado de loading visivel durante request.
- Estado de erro visivel quando API falhar.

## Definicao de pronto
Usuario consegue fazer uma busca valida e receber resposta da API com feedback visual.
