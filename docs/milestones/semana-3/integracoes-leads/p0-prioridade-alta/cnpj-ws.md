# Integracao: CNPJ.ws

## Classificacao
- Prioridade: P0
- Complexidade tecnica: 2/5 (baixa)
- Esforco estimado: 1-2 dias
- Risco operacional: 2/5

## Por que e P0
- Ja existe base de integracao no backend (`cnpj-ws` em allowed-sources).
- Alta aderencia ao mercado brasileiro.
- Custo inicial baixo com plano gratuito (limitado por minuto).

## Escopo tecnico
1. Backend:
- Revisar adapter atual para busca por cidade/CNAE com filtros mais comerciais.
- Adicionar retry/backoff simples e cache curto para evitar limite por IP.
- Instrumentar metricas por fonte (`calls`, `throttle`, `errors`).
2. Frontend:
- Exibir claramente quando o resultado veio de `cnpj-ws`.
- Permitir filtro de fonte no fluxo de busca.

## Dependencias
- Chave/token (se migrar para plano comercial).
- Ajuste de quota no orquestrador de fontes.

## Riscos
- Limite de throughput na API publica.
- Variacao de disponibilidade externa.

## Definition of done
1. Busca retorna leads de `cnpj-ws` com estabilidade e fallback.
2. Quota por fonte ativa e metrica observavel.
3. Testes de contrato do adapter e testes de fallback cobrindo cenarios de erro.

## Proxima acao recomendada
Implementar hardening de resiliencia e quota antes de aumentar volume de chamadas.
