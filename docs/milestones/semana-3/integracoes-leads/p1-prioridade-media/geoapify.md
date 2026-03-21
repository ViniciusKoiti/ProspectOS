# Integracao: Geoapify

## Classificacao
- Prioridade: P1
- Complexidade tecnica: 3/5 (media)
- Esforco estimado: 2-4 dias
- Risco operacional: 3/5

## Por que e P1
- Free tier diario bom para descoberta inicial.
- Custo geralmente competitivo para fluxo de volume medio.
- Qualidade de dados pode variar por regiao e categoria.

## Escopo tecnico
1. Backend:
- Criar source `geoapify` com busca por categoria/localizacao.
- Mapear resposta para o contrato interno de lead.
- Implementar controle de limite diario para nao estourar franquia.
2. Frontend:
- Exibir origem `geoapify` e permitir comparacao por fonte.

## Dependencias
- API key Geoapify.
- Regras de uso comercial/atribuição no plano ativo.

## Riscos
- Limites do plano free para uso comercial mais intenso.
- Dados incompletos para alguns segmentos locais.

## Definition of done
1. Source funcional com testes de contrato.
2. Alertas de quota implementados.
3. Relatorio simples de qualidade da fonte (taxa de website/contato valido).

## Proxima acao recomendada
Posicionar como alternativa de custo controlado antes de escalar para fontes enterprise.
