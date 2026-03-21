# Integracao: OpenCNPJ

## Classificacao
- Prioridade: P0
- Complexidade tecnica: 2/5 (baixa)
- Esforco estimado: 1-3 dias
- Risco operacional: 3/5

## Por que e P0
- Complementa `cnpj-ws` como fallback de baixo custo.
- Boa velocidade para aumentar cobertura de leads no Brasil.
- Permite manter fluxo gratuito inicial.

## Escopo tecnico
1. Backend:
- Criar novo `LeadDiscoverySource` (`open-cnpj`) no modulo de discovery.
- Mapear payload para `DiscoveredLeadCandidate`, preservando `websitePresence`.
- Aplicar rate-limit por fonte e fallback para `cnpj-ws`.
2. Frontend:
- Mostrar `sourceName` no resultado para diagnostico de qualidade por fonte.

## Dependencias
- Contrato HTTP e politica de uso da API comunitaria.
- Configuracao em `prospectos.leads.allowed-sources`.

## Riscos
- Servico comunitario sem SLA formal.
- Possiveis mudancas de payload sem aviso.

## Definition of done
1. Source `open-cnpj` funcional com testes de mapeamento.
2. Fallback automatico funcionando quando `cnpj-ws` falhar.
3. Dashboard de metricas por fonte com taxa de erro.

## Proxima acao recomendada
Implementar como fonte de fallback e nao como unica fonte primaria.

