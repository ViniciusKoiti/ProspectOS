# Integracao: Amazon Location Service

## Classificacao
- Prioridade: P2
- Complexidade tecnica: 4/5 (media-alta)
- Esforco estimado: 4-6 dias
- Risco operacional: 3/5

## Por que e P2
- Bom ecossistema para quem ja opera na AWS.
- Free tier inicial ajuda prova de conceito, mas nao e plano gratuito continuo no longo prazo.
- Overhead de IAM, seguranca e operacao aumenta esforco inicial.

## Escopo tecnico
1. Backend:
- Criar adapter `amazon-location` com autenticacao AWS adequada.
- Mapear resposta para modelo interno de leads.
- Adicionar observabilidade de consumo e custos.
2. Frontend:
- Nenhuma mudanca obrigatoria alem de permitir selecao de fonte.

## Dependencias
- Conta AWS com configuracao de IAM e billing.
- Politica interna para rotacao de credenciais.

## Riscos
- Custos inesperados apos fim da fase gratuita.
- Complexidade operacional maior que alternativas mais simples.

## Definition of done
1. Fonte funcional com credenciais seguras.
2. Controle de consumo e alarmes de custo.
3. Testes de integracao com mocks para evitar dependencia externa em CI.

## Proxima acao recomendada
Considerar apenas se houver estrategia cloud centrada em AWS no produto.
