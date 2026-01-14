---
id: TD-008
title: "Duplicação de métodos toDomainCompany em múltiplas classes"
status: "open"
severity: "low"
type: "maintainability"
module: "cross"
introduced_at: "implementação de testes e serviços de scoring"
tags:
  - "duplication"
  - "maintainability"
  - "refactoring"
  - "dto-mapping"
---

# Contexto
O projeto ProspectOS utiliza Spring Modulith onde o módulo core contém entidades de domínio (Company, ICP) e outros módulos precisam converter DTOs para objetos de domínio. A conversão é necessária para manter as boundaries entre módulos, mas está sendo duplicada.

# Evidências (com caminhos)
- Arquivo: `src/test/java/dev/prospectos/integration/ScoringPersistenceIntegrationTest.java` (linha 150)
  ```java
  private Company toDomainCompany(CompanyDTO dto) { /* implementação */ }
  ```
- Arquivo: `src/test/java/dev/prospectos/integration/ProspectingWorkflowIntegrationTest.java` (linha 163)
  ```java
  private Company toDomainCompany(CompanyDTO company) { /* implementação */ }
  ```
- Arquivo: `src/main/java/dev/prospectos/infrastructure/service/scoring/CompanyScoringService.java`
  ```java
  private Company toDomainCompany(CompanyDTO companyDTO) { /* implementação */ }
  ```

Todas implementam lógica similar de conversão de CompanyDTO → Company.

# Por que isso é um débito técnico
Duplicação de código de conversão cria problemas de manutenibilidade:
- **Código espalhado**: Mesma lógica repetida em vários lugares
- **Inconsistência**: Diferentes implementações podem ter comportamentos sutilmente diferentes
- **Manutenção custosa**: Mudanças na conversão requerem updates em múltiplos lugares
- **Bugs difusos**: Bug em uma implementação pode não ser corrigido em outras
- **Violação DRY**: Don't Repeat Yourself principle violado

# Impacto
- **Impacto técnico**: Código duplicado, manutenção complexa, potencial inconsistência
- **Impacto no produto**: Risk de bugs em conversões, desenvolvimento mais lento
- **Probabilidade**: Baixa (não causa problemas imediatos)
- **Urgência**: Baixa (mais sobre qualidade de código que funcionalidade)

# Estratégias de correção
1. **Opção A (rápida)**: Criar utility class na API
   - Criar `CompanyMapper` em `dev.prospectos.api.mapper`
   - Mover lógica comum para métodos static
   - Refatorar classes existentes para usar mapper
   - Esforço: S (2-3 horas)
   - Prós: Solução rápida, centralizada
   - Contras: Não considera particularidades de cada conversão

2. **Opção B (ideal)**: Implementar strategy pattern com framework
   - Usar MapStruct para mapeamentos automáticos
   - Criar interfaces de mapeamento específicas por contexto
   - Configurar profiles diferentes (test vs production mapping)
   - Esforço: M (5-6 horas)
   - Prós: Type-safe, performance, flexibilidade
   - Contras: Dependência adicional, curva de aprendizado

# Critério de pronto (DoD)
- [ ] Lógica de conversão centralizada
- [ ] Todas as duplicações removidas
- [ ] Testes passam com nova implementação
- [ ] Conversões consistentes entre todas as classes
- [ ] Documentação de como fazer novas conversões
- [ ] Performance mantida ou melhorada
- [ ] Code review para verificar que não há outras duplicações

# Observações
As implementações podem ter pequenas diferenças contextuais (testes vs produção). Importante analisar se todas realmente fazem a mesma coisa antes de consolidar. Também verificar se existe a mesma duplicação para ICP (toDomainIcp). MapStruct seria uma solução robusta mas adiciona dependência - avaliar se vale a pena para este nível de duplicação.