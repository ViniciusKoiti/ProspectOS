---
id: TD-006
title: "Falta de timeouts e retry em integrações AI"
status: "open"
severity: "medium"
type: "reliability"
module: "ai"
introduced_at: "implementação do AIWebSearchScraperClient"
tags:
  - "reliability"
  - "resilience"
  - "ai-integration"
  - "timeout"
  - "retry"
---

# Contexto
O projeto ProspectOS utiliza Spring Modulith com módulo AI que integra com serviços externos (OpenAI, Anthropic) via Spring AI. O módulo AI é crítico para o pipeline de enriquecimento e scoring de empresas, mas não implementa mecanismos de resiliência adequados.

# Evidências (com caminhos)
- Arquivo: `src/main/java/dev/prospectos/ai/client/AIWebSearchScraperClient.java` (linhas 74-76)
  ```java
  String response = chatClient.prompt(prompt)
      .call()
      .content(); // Sem timeout, sem retry
  ```
- Arquivo: `src/main/resources/application.properties` tem configuração básica:
  ```properties
  scraper.ai.timeout=30s
  scraper.ai.max-retries=2
  ```
- Mas estas configurações não são aplicadas no código
- Nenhum circuit breaker ou fallback implementado

# Por que isso é um débito técnico
Integrações AI sem resiliência causam problemas operacionais:
- **Hanging requests**: Calls podem ficar pendentes indefinidamente
- **Cascade failures**: Falha de AI pode derrubar todo o pipeline de enrichment
- **User experience ruim**: Timeouts longos degradam responsividade
- **Resource exhaustion**: Conexões pendentes podem esgotar thread pool
- **Sem fallback**: Sistema quebra completamente quando AI não está disponível

No contexto Spring Modulith, falhas no módulo AI podem afetar outros módulos através dos boundaries.

# Impacto
- **Impacto técnico**: Requests pendentes, resource leaks, sistema menos confiável
- **Impacto no produto**: Timeouts para usuários, pipeline de enrichment instável
- **Probabilidade**: Média (APIs AI ocasionalmente falham ou ficam lentas)
- **Urgência**: Média (sistema funciona, mas não é resiliente)

# Estratégias de correção
1. **Opção A (rápida)**: Implementar timeouts básicos
   - Configurar timeout no ChatClient via properties
   - Adicionar try-catch para timeout exceptions
   - Implementar fallback simples (retornar erro estruturado)
   - Esforço: S (2-3 horas)
   - Prós: Rápido, previne hangs
   - Contras: Não resolve retry nem circuit breaker

2. **Opção B (ideal)**: Implementar resilience patterns completos
   - Usar Resilience4j com Spring Boot
   - Configurar circuit breaker, retry, timeout, rate limiter
   - Implementar fallback strategies (cache, mock responses)
   - Adicionar métricas de health para cada provider AI
   - Esforço: M (6-8 horas)
   - Prós: Sistema altamente resiliente
   - Contras: Complexidade adicional

# Critério de pronto (DoD)
- [ ] Timeouts configurados em todas as chamadas AI
- [ ] Retry policy implementado com backoff exponencial
- [ ] Circuit breaker configurado para cada provider AI
- [ ] Fallback responses implementados
- [ ] Métricas de reliability expostas via Actuator
- [ ] Testes de integração para cenários de falha
- [ ] Configuração por environment (dev, staging, prod)

# Observações
As configurações em application.properties (timeout=30s, max-retries=2) sugerem que havia intenção de implementar resiliência, mas não foram aplicadas. Spring AI pode ter configurações nativas de timeout que precisam ser investigadas. Considerar usar @Retryable e @CircuitBreaker do Spring Retry como primeira opção antes de adicionar Resilience4j.