# Integracao: OpenCage

## Classificacao
- Prioridade: P2
- Complexidade tecnica: 2/5 (baixa)
- Esforco estimado: 1-2 dias
- Risco operacional: 4/5

## Por que e P2
- Integracao simples para testes rapidos.
- Plano gratuito e orientado a trial, nao a escala de producao.
- Pode ser util como fonte secundaria para validacao.

## Escopo tecnico
1. Backend:
- Criar source `opencage` com limites estritos de uso.
- Mapear campos para candidatos de lead.
- Tratar erros de rate-limit com fallback para fontes P0/P1.
2. Frontend:
- Exibir origem e status de disponibilidade da fonte.

## Dependencias
- API key OpenCage.
- Politica de uso do plano atual.

## Riscos
- Limites de trial inviabilizam volume real.
- Mudanca de custo apos fase inicial.

## Definition of done
1. Source funcional para experimento de baixo volume.
2. Fallback automatico para outras fontes ao atingir limite.
3. Monitoramento de limite e erro por fonte.

## Proxima acao recomendada
Usar como apoio de teste comparativo, nao como fonte principal.
