# Roadmap Fluxo A (On-demand)

## Objetivo
Implementar o fluxo de prospeccao on-demand para gerar leads a partir de uma pesquisa do usuario.

## Fase 1 — Alinhamento (1–2 dias)
- Definir ICP inicial (ex.: agronomos/fazendeiros do PR).
- Definir fontes permitidas (TOS/robots).
- Definir politica LGPD (base legal + opt-out).

## Fase 2 — Modelo de dados (2–3 dias)
- Decidir: estender `Company` ou criar entidades auxiliares.
- Definir campos minimos: origem do lead, data, status, opt-out.
- Desenhar migracoes iniciais.

## Fase 3 — Pipeline de scraping (4–7 dias)
- Implementar scraper minimo (1–2 fontes).
- Normalizar dados (nome, empresa, site).
- Registrar origem e timestamp.

## Fase 4 — Enriquecimento e validacao (3–5 dias)
- Integrar provedor de contatos.
- Validar email (bounce check).
- Deduplicar contatos.

## Fase 5 — Scoring com IA (3–4 dias)
- Aplicar score conforme ICP.
- Armazenar score e analise.

## Fase 6 — API e busca (4–6 dias)
- Endpoints para consulta e filtros.
- Paginacao e busca por regiao/segmento.

## Fase 7 — Compliance e monitoramento (2–3 dias)
- Opt-out centralizado.
- Retencao e auditoria de origem.
- Logs minimos.

## Fase 8 — Testes (2–4 dias)
- Unit tests no core.
- Integration tests do pipeline.

## Entregaveis esperados
- Pipeline on-demand funcional.
- Leads com origem rastreavel.
- Score e recomendacoes geradas por IA.
- API pronta para consumo.
