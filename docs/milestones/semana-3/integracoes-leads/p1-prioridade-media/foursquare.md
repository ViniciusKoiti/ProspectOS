# Integracao: Foursquare Places API

## Classificacao
- Prioridade: P1
- Complexidade tecnica: 3/5 (media)
- Esforco estimado: 3-5 dias
- Risco operacional: 3/5

## Por que e P1
- Boa cobertura e qualidade para POI comercial.
- Free tier inicial util, mas recursos premium exigem controle de custo.
- Exige ajuste de payload para modelo interno de leads.

## Escopo tecnico
1. Backend:
- Criar source `foursquare` no registry de discovery.
- Mapear dados de place para `DiscoveredLeadCandidate`.
- Aplicar quota por minuto e por dia.
- Instrumentar qualidade por fonte (website, telefone, localizacao).
2. Frontend:
- Expor filtro por fonte e visibilidade de proveniencia.

## Dependencias
- API key Foursquare.
- Politica de uso e atribuicao conforme contrato.

## Riscos
- Campos premium aumentam custo efetivo por lead.
- Possivel variacao de cobertura em cidades menores.

## Definition of done
1. Fonte `foursquare` integrada com testes de contrato e mapeamento.
2. Quota e fallback por fonte ativos.
3. Observabilidade de erro e consumo de chamadas.

## Proxima acao recomendada
Rodar piloto controlado por cidade e comparar custo por lead qualificado contra TomTom/Google.
