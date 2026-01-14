---
id: TD-005
title: "Uso de Optional.orElse(null) violando práticas de Optional"
status: "open"
severity: "medium"
type: "design"
module: "infrastructure"
introduced_at: "implementação dos serviços JPA"
tags:
  - "optional"
  - "null-safety"
  - "api-design"
  - "best-practices"
---

# Contexto
O projeto ProspectOS utiliza Spring Modulith onde o módulo core deve ser dependency-free. Os serviços de infraestrutura implementam interfaces da API e precisam retornar objetos ou null de forma consistente, seguindo práticas de null-safety do Java moderno.

# Evidências (com caminhos)
- Arquivo: `src/main/java/dev/prospectos/infrastructure/service/jpa/CompanyDataServiceJpa.java` (linha 41)
  ```java
  return companyRepository.findById(companyId)
      .map(this::toDTO)
      .orElse(null);
  ```
- Arquivo: `src/main/java/dev/prospectos/infrastructure/service/jpa/ICPDataServiceJpa.java` (linha similar)
  ```java
  .orElse(null);
  ```
- Arquivo: `src/test/java/dev/prospectos/integration/ScoringPersistenceIntegrationTest.java` (linha 87)
  ```java
  Company retrievedCompany = companyRepository.findById(savedCompany.getId()).orElse(null);
  ```

# Por que isso é um débito técnico
O padrão `.orElse(null)` derrota o propósito do Optional e introduz riscos:
- **Null-safety perdida**: Optional foi criado justamente para evitar nulls
- **NullPointerException**: Callers podem não verificar null antes de usar
- **API inconsistente**: Mistura paradigmas Optional com null-based
- **Documentação implícita perdida**: Optional sinaliza que valor pode estar ausente
- **Debugging complexo**: NPEs podem aparecer longe do ponto real do problema

# Impacto
- **Impacto técnico**: Potenciais NPEs, API confusa, perda de type safety
- **Impacto no produto**: Bugs difíceis de detectar, experiência inconsistente para desenvolvedores
- **Probabilidade**: Média (depende dos paths de código executados)
- **Urgência**: Média (não causa problemas imediatos, mas degrada qualidade)

# Estratégias de correção
1. **Opção A (rápida)**: Retornar Optional nas interfaces
   - Mudar assinaturas dos métodos para retornar Optional<CompanyDTO>
   - Atualizar implementações para retornar Optional.empty() em vez de null
   - Ajustar callers para usar Optional.ifPresent() ou .map()
   - Esforço: S (2-3 horas)
   - Prós: Null-safety correta, API clara
   - Contras: Breaking change nas interfaces

2. **Opção B (ideal)**: Estratégia híbrida com validação
   - Manter null para compatibilidade em APIs públicas
   - Usar Optional internamente nos repositórios
   - Adicionar validação null-check nos callers
   - Documentar comportamento com @Nullable/@NonNull annotations
   - Esforço: M (4-5 horas)
   - Prós: Compatibilidade mantida, melhor documentação
   - Contras: Não resolve completamente o problema

# Critério de pronto (DoD)
- [ ] Interfaces de serviços definidas consistentemente (Optional ou null)
- [ ] Implementações seguem padrão escolhido
- [ ] Callers adaptados para padrão escolhido
- [ ] Annotations @Nullable/@NonNull adicionadas onde apropriado
- [ ] Testes atualizar para verificar comportamento correto
- [ ] Documentação de API atualizada
- [ ] Nenhum .orElse(null) em código de produção

# Observações
O uso em testes (ScoringPersistenceIntegrationTest.java) é menos crítico, mas ainda demonstra o padrão ruim. Considerar criar utility methods como `findByIdOrThrow()` para casos onde null não é esperado. A interface CompanyDataService precisa ser revista para definir contrato claro sobre retorno de nulls vs Optionals.