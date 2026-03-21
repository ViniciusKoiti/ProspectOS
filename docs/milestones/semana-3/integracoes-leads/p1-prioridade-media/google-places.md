# Integracao: Google Places (Maps Platform)

## Classificacao
- Prioridade: P1
- Complexidade tecnica: 4/5 (media-alta)
- Esforco estimado: 4-7 dias
- Risco operacional: 4/5

## Por que e P1
- Qualidade de dados muito alta para descoberta local.
- Custos podem subir rapido dependendo de field mask e volume.
- Requer disciplina forte de compliance e observabilidade de billing.

## Escopo tecnico
1. Backend:
- Criar `LeadDiscoverySource` (`google-places`) com `Text Search` e/ou `Nearby`.
- Implementar estrategia de consulta em duas etapas para reduzir custo: etapa 1 com campos basicos, etapa 2 com enriquecimento seletivo.
- Guardar metrica de custo estimado por request/SKU.
- Aplicar limites por job para evitar bursts caros.
2. Frontend:
- Expor fonte `google-places` apenas para cenarios premium/selecionados.
- Sinalizar ao usuario quando a fonte estiver em modo "alto custo".

## Dependencias
- Chave de API e configuracao de billing.
- Regras claras de uso de dados e storage conforme politicas.

## Riscos
- Drift de custo por mudanca de field mask.
- Bloqueio de quotas/projeto em caso de picos.
- Complexidade de governanca de dados.

## Definition of done
1. Fonte funcional com monitoramento de custo por chamada.
2. Controles de quota/limite configuraveis por ambiente.
3. Testes de regressao cobrindo mapeamento, fallback e erro de quota.

## Proxima acao recomendada
Usar Google como enriquecimento seletivo de leads priorizados (nao como varredura massiva inicial).
