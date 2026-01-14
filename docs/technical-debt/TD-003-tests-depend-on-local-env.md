---
id: TD-003
title: "Testes de integração dependem de variáveis de ambiente locais"
status: "open"
severity: "high"
type: "test"
module: "cross"
introduced_at: "implementação dos testes de integração AI"
tags:
  - "testing"
  - "environment"
  - "ci-cd"
  - "reliability"
---

# Contexto
O projeto ProspectOS utiliza Spring Modulith com testes que validam boundaries entre módulos (core, api, infrastructure, ai). A arquitetura requer que testes sejam determinísticos e independentes de configurações locais para garantir a integridade dos boundaries.

# Evidências (com caminhos)
- Arquivo: `src/test/java/dev/prospectos/integration/AIServicesIntegrationTest.java`
  ```java
  @TestPropertySource(locations = "file:.env")
  ```
- Arquivo: `src/test/java/dev/prospectos/integration/ProspectingWorkflowIntegrationTest.java`
  ```java
  @TestPropertySource(locations = "file:.env")
  ```
- Arquivo: `src/test/java/dev/prospectos/integration/ProspectEnrichmentIntegrationTest.java`
  ```java
  @TestPropertySource(locations = "file:.env")
  ```
- Arquivo: `src/test/resources/application-test.properties` tem fallbacks:
  ```properties
  spring.ai.openai.api-key=${OPENAI_API_KEY:dummy-key}
  spring.ai.anthropic.api-key=${ANTHROPIC_API_KEY:dummy-key}
  ```

# Por que isso é um débito técnico
Dependência de arquivo .env local em testes cria problemas sistêmicos:
- **Builds não-determinísticos**: Comportamento varia entre desenvolvedores
- **CI/CD quebrado**: Pipelines falham sem arquivo .env local
- **Debugging complexo**: Falhas podem ser específicas de ambiente
- **Onboarding difícil**: Novos desenvolvedores precisam configurar .env específico
- **Violação de princípios**: Testes devem ser isolados e reprodutíveis

# Impacto
- **Impacto técnico**: Testes instáveis, pipelines de CI/CD quebrados, debugging complexo
- **Impacto no produto**: Regressões não detectadas, deploy de bugs, baixa confiança nos testes
- **Probabilidade**: Alta (toda execução de teste depende do .env local)
- **Urgência**: Alta (bloqueia pipelines e automação)

# Estratégias de correção
1. **Opção A (rápida)**: Usar apenas application-test.properties
   - Remover `@TestPropertySource(locations = "file:.env")` de todos os testes
   - Garantir que application-test.properties tem todas as configurações necessárias
   - Usar valores mock/dummy por padrão
   - Esforço: S (2-3 horas)
   - Prós: Solução rápida, testes determinísticos
   - Contras: Pode perder algumas configurações específicas

2. **Opção B (ideal)**: Estratégia de testes em camadas
   - Testes unitários: 100% mocks, sem integrações externas
   - Testes de integração: application-test.properties com mocks
   - Testes E2E: variáveis de ambiente opcionais para testes com APIs reais
   - Profiles específicos (test, integration, e2e)
   - Esforço: M (5-7 horas)
   - Prós: Flexibilidade total, CI/CD robusto
   - Contras: Configuração mais complexa

# Critério de pronto (DoD)
- [ ] Todos os testes passam sem arquivo .env presente
- [ ] application-test.properties contém todas as configurações necessárias
- [ ] CI/CD pipeline configurado e funcionando
- [ ] Testes de integração usam mocks por padrão
- [ ] Testes E2E opcionais com configuração via variáveis de ambiente
- [ ] Documentação atualizada sobre execução de testes
- [ ] Boundary tests (Modulith) continuam passando

# Observações
Os fallbacks em application-test.properties (dummy-key) já existem, mas não são suficientes porque o @TestPropertySource sobrescreve. A remoção do `file:.env` deve fazer os testes usarem os mocks configurados. Verificar se existem testes que realmente precisam de APIs reais vs. podem usar mocks.