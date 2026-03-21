# Levantamento de Integracoes com Plano Gratuito para Leads (2026-03-21)

## Objetivo
Mapear integracoes uteis para descoberta de empresas com foco em custo zero inicial (ou custo muito baixo), priorizando fontes com limites gratuitos explicitos na documentacao oficial.

## Resumo executivo
- Melhor custo/beneficio inicial para o contexto atual: `cnpj-ws` + `opencnpj` + `tomtom`.
- `google places` continua util, mas deve ser usado de forma seletiva para nao escalar custo.
- Fontes publicas como `nominatim` sao boas para baixo volume, mas nao para carga alta no endpoint publico.

## Desdobramento detalhado por prioridade
- Visao geral de priorizacao: `docs/milestones/semana-3/integracoes-leads/README.md`
- Pasta P0: `docs/milestones/semana-3/integracoes-leads/p0-prioridade-alta/`
- Pasta P1: `docs/milestones/semana-3/integracoes-leads/p1-prioridade-media/`
- Pasta P2: `docs/milestones/semana-3/integracoes-leads/p2-prioridade-baixa-ou-trial/`

## Integracoes com plano gratuito confirmado
| Integracao | Modelo gratuito | Limite gratis observado | Adequacao para o produto | Ponto de atencao |
| --- | --- | --- | --- | --- |
| Google Maps Platform (Places API New) | Free usage cap por categoria de SKU | Essentials: 10.000/mes; Pro: 5.000/mes; Enterprise: 1.000/mes; alguns SKUs IDs Only com `Unlimited` | Alta (cobertura e qualidade) | Custo sobe rapido ao pedir campos Enterprise (ex: `websiteUri`, telefone) |
| Foursquare Places API | Freemium por volume | Pro endpoints: 0-10.000 calls com `$0.00 CPM` | Alta para POI e busca comercial | Campos Premium sao cobrados |
| TomTom APIs | Freemium diario | 2.500 non-tile/dia e 50.000 tile/dia (franquia compartilhada) | Alta para descoberta inicial com baixo custo | Limite diario precisa controle de quota no backend |
| Geoapify | Freemium diario | 3.000 credits/dia | Media/Alta, boa opcao de custo | Plano Free com uso comercial limitado |
| CNPJ.ws (API publica) | API publica gratuita | 3 requisicoes/minuto por IP | Alta para Brasil (enriquecimento CNPJ) | Throughput baixo sem plano comercial |
| OpenCNPJ | API comunitaria gratuita | 100 requisicoes/minuto | Alta para Brasil como fallback | Projeto comunitario, sem SLA enterprise |
| OpenStreetMap Nominatim (publico) | Servico publico gratuito | Maximo absoluto de 1 req/s + sem heavy usage | Media para baixo volume | Nao recomendado para carga alta; politica restritiva |

## Integracoes com "trial" (nao free continuo)
- Amazon Location Service: free tier inicial e creditos para contas novas, depois cobranca por uso.
- OpenCage: free trial para testes, com restricoes de uso no plano gratuito.

## Recomendacao free-first para o produto
1. Fase 1 (imediata): `cnpj-ws` + `opencnpj` + `tomtom`, com rate-limit por fonte e fallback por prioridade.
2. Fase 2 (otimizacao): `google places` apenas para enriquecimento dos leads ja priorizados por score.
3. Fase 3 (escala): definir criterio de upgrade por fonte com base em conversao e custo por lead qualificado.

## Recomendacao tecnica para o backend atual
1. Adicionar suporte de quota por `sourceName` no fluxo de descoberta.
2. Registrar metrica por fonte: `calls`, `throttle`, `cost_estimate`, `qualified_leads`.
3. Implementar estrategia de fallback ordenada por custo: `opencnpj`/`cnpj-ws` -> `tomtom` -> `foursquare` -> `google places`.
4. Garantir que cada fonte preencha `websitePresence` para manter filtros e outreach segmentado.

## Riscos de compliance e operacao
- Google Places: billing por field mask no maior SKU da requisicao.
- Nominatim publico: politica de uso estrita (nao usar para carga alta).
- APIs comunitarias: risco de indisponibilidade ou mudanca de limite sem aviso.

## Fontes oficiais consultadas
- Google pricing categories: https://developers.google.com/maps/billing-and-pricing/pricing-categories
- Google pricing table (global): https://developers.google.com/maps/billing-and-pricing/pricing
- Google SKU details: https://developers.google.com/maps/billing-and-pricing/sku-details
- Foursquare pricing: https://foursquare.com/pricing/
- TomTom pricing: https://docs.tomtom.com/pricing
- TomTom FAQ (limites diarios): https://developer.tomtom.com/platform/documentation/faqs
- Geoapify pricing: https://www.geoapify.com/pricing/
- CNPJ.ws API publica (consulta): https://docs.cnpj.ws/referencia-de-api/api-publica/consultando-cnpj
- CNPJ.ws API publica (limitacoes): https://docs.cnpj.ws/referencia-de-api/api-publica/limitacoes
- OpenCNPJ: https://opencnpj.com/
- Nominatim usage policy: https://operations.osmfoundation.org/policies/nominatim/
- Amazon Location pricing: https://aws.amazon.com/location/pricing/
- OpenCage pricing: https://opencagedata.com/pricing
