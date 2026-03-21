# Planejamento de Integracoes de Leads por Prioridade (2026-03-21)

## Escala usada
- Complexidade tecnica: 1 (muito baixa) ate 5 (muito alta)
- Esforco: dias uteis para primeira entrega funcional
- Risco operacional: 1 (baixo) ate 5 (alto)

## Matriz comparativa
| Integracao | Complexidade | Esforco | Risco operacional | Prioridade |
| --- | --- | --- | --- | --- |
| CNPJ.ws | 2/5 | 1-2 dias | 2/5 | P0 |
| OpenCNPJ | 2/5 | 1-3 dias | 3/5 | P0 |
| TomTom | 3/5 | 3-5 dias | 2/5 | P0 |
| Google Places | 4/5 | 4-7 dias | 4/5 | P1 |
| Foursquare | 3/5 | 3-5 dias | 3/5 | P1 |
| Geoapify | 3/5 | 2-4 dias | 3/5 | P1 |
| Nominatim (publico) | 2/5 | 1-2 dias | 5/5 | P2 |
| Amazon Location | 4/5 | 4-6 dias | 3/5 | P2 |
| OpenCage | 2/5 | 1-2 dias | 4/5 | P2 |

## Ordem recomendada
1. P0: custo baixo e valor rapido para gerar base de leads.
2. P1: fontes com melhor qualidade/completude, mas com custo e compliance mais sensiveis.
3. P2: trial/uso restrito, util para teste controlado e validacao pontual.

## Pastas
- `p0-prioridade-alta`
- `p1-prioridade-media`
- `p2-prioridade-baixa-ou-trial`

## Objetivo de negocio
Priorizar fontes que ajudam desenvolvedores a identificar empresas com maior chance de compra de servicos web, com foco em fluxo gratuito inicial e controle de custo.
