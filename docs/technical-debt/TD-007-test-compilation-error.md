---
id: TD-007
title: "Erro sintático em teste de integração de enrichment"
status: "open"
severity: "medium"
type: "test"
module: "core"
introduced_at: "criação dos testes de enrichment pipeline"
tags:
  - "compilation"
  - "syntax-error"
  - "test"
  - "enrichment"
---

# Contexto
O projeto ProspectOS utiliza Spring Modulith com testes que validam o pipeline de enriquecimento. O módulo core contém a lógica de enrichment que deve ser testada adequadamente para garantir que as regras de negócio funcionem corretamente.

# Evidências (com caminhos)
- Arquivo: `src/test/java/dev/prospectos/integration/EnrichmentPipelineIntegrationTest.java` (linha 143)
  ```java
  EnrichmentResult result = enrichmentService.enrichmentService.enrichCompanyData(request);
  //                                        ^^^^^^^^^^^^^^^^^
  // ERRO: "enrichmentService" duplicado
  ```

A linha deveria ser:
```java
EnrichmentResult result = enrichmentService.enrichCompanyData(request);
```

# Por que isso é um débito técnico
Erro sintático em teste impacta o desenvolvimento:
- **Build quebrado**: Compilação falha, bloqueando outros desenvolvedores
- **CI/CD quebrado**: Pipelines não conseguem rodar testes
- **Cobertura perdida**: Funcionalidade não está sendo testada
- **Confiança reduzida**: Sugere falta de validação antes de commit
- **Tempo perdido**: Desenvolvedores perdem tempo debugando erro óbvio

No contexto Spring Modulith, é crítico que testes de boundary passem para validar a arquitetura modular.

# Impacto
- **Impacto técnico**: Build quebrado, testes não executam, cobertura de código reduzida
- **Impacto no produto**: Funcionalidade de enrichment não validada, potenciais bugs não detectados
- **Probabilidade**: Muito alta (erro sempre ocorre na compilação)
- **Urgência**: Média (bloqueia testes mas não afeta produção diretamente)

# Estratégias de correção
1. **Opção A (rápida)**: Corrigir erro sintático
   - Remover duplicação ".enrichmentService" da linha 143
   - Testar compilação e execução do teste
   - Esforço: S (5 minutos)
   - Prós: Fix imediato, build volta a funcionar
   - Contras: Não previne futuros erros similares

2. **Opção B (ideal)**: Fix + melhoria do processo
   - Corrigir erro sintático
   - Configurar pre-commit hooks para compilação
   - Adicionar validação de build no CI/CD
   - Code review obrigatório para testes
   - Esforço: S-M (1-2 horas)
   - Prós: Previne futuros problemas similares
   - Contras: Setup adicional de processo

# Critério de pronto (DoD)
- [ ] Erro sintático corrigido
- [ ] Teste compila sem errors
- [ ] Teste executa e passa
- [ ] Funcionalidade de enrichment adequadamente coberta
- [ ] Build completo (incluindo testes) executa sem errors
- [ ] CI/CD pipeline configurado para falhar em compilation errors
- [ ] Pre-commit hook configurado (opcional)

# Observações
Este é um erro simples mas que demonstra falta de validação antes de commit. O teste parece ter sido criado recentemente para validar o pipeline MVP-004 de enrichment. Importante verificar se outros testes similares não têm problemas parecidos. Considerar usar IDEs com validação automática e configurar formatação automática.