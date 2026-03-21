# Integracao: OpenStreetMap Nominatim (endpoint publico)

## Classificacao
- Prioridade: P2
- Complexidade tecnica: 2/5 (baixa)
- Esforco estimado: 1-2 dias
- Risco operacional: 5/5

## Por que e P2
- Integra facil, mas o endpoint publico tem politica muito restritiva.
- Serve para validacao de conceito e baixo volume.
- Nao e caminho seguro para escala em producao usando endpoint publico.

## Escopo tecnico
1. Backend:
- Criar source `nominatim-public` com limites bem conservadores.
- Bloquear uso em lote pesado por configuracao.
- Preparar caminho de migracao para instancia propria/provedor dedicado.
2. Frontend:
- Mostrar estado "uso restrito" quando fonte estiver ativa.

## Dependencias
- Politica de uso do servico publico.
- Eventual infraestrutura para self-host no futuro.

## Riscos
- Bloqueio de IP por violacao de uso.
- Instabilidade por ser endpoint publico compartilhado.

## Definition of done
1. Source disponivel apenas para volume baixo e testes.
2. Guardrails de limite habilitados por padrao.
3. Documentacao de migracao para alternativa escalavel.

## Proxima acao recomendada
Usar apenas para experimento controlado; nao adotar como pilar de producao sem self-host.
