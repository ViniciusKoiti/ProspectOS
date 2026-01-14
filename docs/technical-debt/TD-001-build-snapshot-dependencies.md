---
id: TD-001
title: "Build dependencies using SNAPSHOT and Milestone versions"
status: "open"
severity: "critical"
type: "build"
module: "cross"
introduced_at: "initial project setup"
tags:
  - "build"
  - "dependencies"
  - "stability"
  - "production"
---

# Contexto
O projeto ProspectOS utiliza uma arquitetura modular monolítica com Spring Modulith, onde a estabilidade das dependências é crucial para garantir que os boundaries entre módulos (core, api, infrastructure, ai) funcionem corretamente. O core deve permanecer dependency-free de outros módulos.

# Evidências (com caminhos)
- Arquivo: `build.gradle` (linha 3)
  ```gradle
  id 'org.springframework.boot' version '3.5.10-SNAPSHOT'
  ```
- Arquivo: `build.gradle` (linha 26)
  ```gradle
  set('springAiVersion', "1.0.0-M4")
  ```

# Por que isso é um débito técnico
Versões SNAPSHOT e Milestone são versões de desenvolvimento/preview que podem:
- Mudar comportamento entre builds sem aviso
- Introduzir bugs não documentados
- Ter APIs instáveis que quebram a compatibilidade
- Não ter suporte oficial em produção
- Comprometer a reprodutibilidade de builds

No contexto de uma arquitetura modulith, mudanças inesperadas podem quebrar os contratos entre módulos, especialmente as validações críticas do ModulithTest.

# Impacto
- **Impacto técnico**: Builds não determinísticos, possíveis quebras de API em runtime, falhas de testes Modulith
- **Impacto no produto**: Instabilidade em produção, dificuldade de debugging, rollbacks complexos
- **Probabilidade**: Alta (versões SNAPSHOT mudam constantemente)
- **Urgência**: Crítica (afeta estabilidade de produção)

# Estratégias de correção
1. **Opção A (rápida)**: Migrar para versões stable/GA mais próximas
   - Spring Boot 3.3.x ou 3.4.x (GA)
   - Spring AI 1.0.0-M3 → investigar versão mais estável
   - Esforço: S (1-2 horas)
   - Prós: Rápido, menor risco
   - Contras: Pode perder features recentes

2. **Opção B (ideal)**: Estratégia de versioning híbrida
   - Manter SNAPSHOTs apenas em desenvolvimento
   - Usar versões GA/Release em staging/produção
   - Configurar profiles gradle específicos
   - Esforço: M (4-6 horas)
   - Prós: Flexibilidade, controle total
   - Contras: Configuração mais complexa

# Critério de pronto (DoD)
- [ ] Todas as dependências principais usam versões GA/Release
- [ ] Build reprodutível entre execuções
- [ ] Testes Modulith continuam passando
- [ ] Profiles para dev/staging/prod com controle de versão
- [ ] Documentação atualizada em CLAUDE.md

# Observações
Spring Boot 3.5.x ainda está em preview. Considerar downgrade para 3.3.x LTS que oferece suporte estendido. Spring AI 1.0.0-M4 é Milestone, verificar roadmap para versão GA.