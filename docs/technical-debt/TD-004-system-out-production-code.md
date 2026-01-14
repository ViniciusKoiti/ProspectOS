---
id: TD-004
title: "Uso de System.out em código de produção"
status: "open"
severity: "high"
type: "observability"
module: "ai"
introduced_at: "implementação do TokenUsageMonitor"
tags:
  - "logging"
  - "observability"
  - "production"
  - "monitoring"
---

# Contexto
O projeto ProspectOS utiliza arquitetura Spring Modulith com módulos isolados. O módulo AI precisa de observabilidade adequada para monitorar uso de tokens e custos, mas atualmente usa System.out em vez de sistema de logging estruturado.

# Evidências (com caminhos)
- Arquivo: `src/main/java/dev/prospectos/ai/monitoring/TokenUsageMonitor.java` (linhas 32-37)
  ```java
  System.out.println("\n📊 === Token Usage Summary ===");
  System.out.println("Total Requests: " + totalRequests.get());
  System.out.println("Estimated Tokens: ~" + estimatedTokens.get());
  System.out.println("Estimated Cost: ~$" + String.format("%.4f", estimatedTokens.get() * 0.00002));
  System.out.println("===============================\n");
  ```
- Arquivo: `src/main/java/dev/prospectos/ai/example/SimpleAIDemo.java` (múltiplas linhas)
  ```java
  System.out.println("=== ProspectOS AI Demo (Using Mocks) ===\n");
  System.out.println("Provider: " + provider.getClient().getProvider().getDisplayName());
  // ... mais 10+ ocorrências
  ```

# Por que isso é um débito técnico
System.out em produção compromete observabilidade e operações:
- **Não-estruturado**: Logs ficam misturados com saída padrão
- **Sem níveis**: Impossível filtrar por criticidade (DEBUG, INFO, ERROR)
- **Sem contexto**: Não há correlação de requests, trace IDs, ou metadata
- **Performance**: System.out é síncrono e pode degradar performance
- **Monitoramento**: Ferramentas APM não conseguem coletar métricas adequadamente
- **Debugging**: Dificulta investigação de problemas em produção

# Impacto
- **Impacto técnico**: Logs não-estruturados, debugging complexo, monitoramento prejudicado
- **Impacto no produto**: Dificuldade de troubleshooting, custos AI não monitorados adequadamente
- **Probabilidade**: Alta (código roda em todas as operações AI)
- **Urgência**: Alta (afeta observabilidade de produção)

# Estratégias de correção
1. **Opção A (rápida)**: Migrar para SLF4J Logger
   - Substituir System.out por log.info() no TokenUsageMonitor
   - Manter SimpleAIDemo como está (é apenas demo/exemplo)
   - Configurar nível INFO para logs de monitoring
   - Esforço: S (1-2 horas)
   - Prós: Rápido, mantém funcionalidade
   - Contras: Não aproveita todo potencial de observabilidade

2. **Opção B (ideal)**: Implementar observabilidade estruturada
   - Usar Micrometer para métricas de token usage
   - Implementar structured logging com MDC para correlação
   - Adicionar dashboards/alertas para custos AI
   - Integrar com Spring Boot Actuator
   - Esforço: M (4-6 horas)
   - Prós: Observabilidade completa, métricas, alertas
   - Contras: Setup mais complexo

# Critério de pronto (DoD)
- [ ] TokenUsageMonitor usa Logger em vez de System.out
- [ ] Logs estruturados com formato JSON em produção
- [ ] Métricas de token usage expostas via Actuator
- [ ] Configuração de níveis de log por environment
- [ ] SimpleAIDemo marcado como @Profile("demo") ou movido para test
- [ ] Testes passam sem poluição de console
- [ ] Documentação de observabilidade adicionada

# Observações
TokenUsageMonitor é classe de produção e deve usar Logger. SimpleAIDemo parece ser código de exemplo/demo e poderia ser movido para testes ou configurado com @Profile("demo"). O uso de emojis nos logs (📊) pode ser problemático em alguns ambientes - considerar remover em produção.