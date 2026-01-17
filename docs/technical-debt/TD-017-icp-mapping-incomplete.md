# TD-017: Mapeamento incompleto de ICP para scoring

**Status:** OPEN  
**Priority:** P1 (MEDIUM/HIGH - Correctness)  
**Created:** 2025-01-17  
**Module:** `infrastructure.service.scoring`

## Problem

`CompanyScoringService.toDomainICP()` mapeia ICP incompletamente: `regions` vira lista vazia e `interestTheme` vira `null`.

### Evidence

```java
// CompanyScoringService.java
private ICP toDomainICP(ICPDto icpDTO) {
    return ICP.create(
        icpDTO.name(),
        icpDTO.description(),
        icpDTO.targetIndustries(),
        List.of(),  // ❌ regions perdidas
        icpDTO.targetRoles(),
        null  // ❌ interestTheme perdido
    );
}
```

Mas o scoring AI **usa** regions e theme:
```java
// ScoringAIService.java
String.join(", ", icp.getRegions()),  // ❌ sempre vazio
icp.getInterestTheme()  // ❌ sempre null
```

### Impact

- Scoring AI recebe dados incompletos
- Qualidade do score é reduzida
- Prompts ficam vazios em campos importantes
- Inconsistência entre ICP persistido e ICP usado no scoring

## Solution

Mapear ICP completamente:

```java
private ICP toDomainICP(ICPDto icpDTO) {
    return ICP.create(
        icpDTO.name(),
        icpDTO.description(),
        icpDTO.targetIndustries(),
        icpDTO.regions() != null ? icpDTO.regions() : List.of(),  // ✅
        icpDTO.targetRoles(),
        icpDTO.interestTheme()  // ✅
    );
}
```

**Mas:** `ICPDto` hoje não tem `regions` nem `interestTheme`.

### Root Cause

`ICPDto` está incompleto. Campos presentes em `ICP` domain mas ausentes em DTO:
- `regions` (existe no domain, ausente no DTO)
- `interestTheme` (existe no domain, ausente no DTO)

### Full Solution

1. Adicionar campos faltantes em `ICPDto`:
   ```java
   public record ICPDto(
       // ... campos existentes
       List<String> regions,      // ✅ adicionar
       String interestTheme       // ✅ adicionar
   ) {}
   ```

2. Atualizar `ICPCreateRequest` e `ICPUpdateRequest`

3. Atualizar serviços JPA e in-memory para mapear corretamente

4. Atualizar `toDomainICP` para usar novos campos

### Implementation Steps

1. Adicionar `regions` e `interestTheme` em `ICPDto`
2. Adicionar nos request DTOs (`ICPCreateRequest`, `ICPUpdateRequest`)
3. Atualizar `ICPDataServiceJpa.toDTO()`
4. Atualizar `InMemoryICPDataService` (seed data)
5. Atualizar `CompanyScoringService.toDomainICP()`
6. Atualizar testes

### Acceptance Criteria

- [x] `ICPDto` tem `regions` e `interestTheme`
- [x] Serviços mapeiam corretamente
- [x] Scoring AI recebe ICP completo
- [x] Testes validam que dados não são perdidos

## Dependencies

- TD-013 (scoring preview) se beneficia deste fix

## Notes

Este fix melhora qualidade do scoring significativamente. É importante para o produto entregar valor real (score preciso).
