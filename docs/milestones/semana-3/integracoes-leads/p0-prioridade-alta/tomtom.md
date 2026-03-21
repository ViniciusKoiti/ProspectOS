# Integracao: TomTom Search/Places

## Classificacao
- Prioridade: P0
- Complexidade tecnica: 3/5 (media)
- Esforco estimado: 3-5 dias
- Risco operacional: 2/5

## Por que e P0
- Free tier diario relevante para fase inicial.
- Boa cobertura para busca de negocios locais.
- Custo previsivel e menor que alternativas enterprise para enriquecimento amplo.

## Escopo tecnico
1. Backend:
- Criar adapter `tomtom` como `LeadDiscoverySource`.
- Implementar mapeamento de POI para candidato de lead.
- Configurar throttle por dia e por minuto no orquestrador.
- Registrar score de qualidade por fonte (contato valido, website, localizacao).
2. Frontend:
- Habilitar selecao da fonte `tomtom`.
- Exibir badge de fonte e status de quota (opcional, admin).

## Dependencias
- Chave da API TomTom.
- Parametrizacao de regiao, idioma e raio de busca.

## Riscos
- Consumo rapido de cota diaria sem controle de pagina/limite.
- Variação de cobertura por cidade.

## Definition of done
1. Source `tomtom` configuravel e funcional em ambiente de desenvolvimento.
2. Rate-limit e logs de quota ativos.
3. Testes de contrato HTTP + testes de mapeamento de campos.

## Proxima acao recomendada
Implementar com modo "quota-safe" por padrao (limites conservadores) para evitar esgotar franquia.
