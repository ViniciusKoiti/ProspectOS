# TD-013: Scoring acoplado a companyId persistido

**Status:** OPEN  
**Priority:** P0 (CRITICAL - Architecture)  
**Created:** 2025-01-17  
**Module:** `infrastructure.service.scoring`

## Problem

`CompanyScoringService.scoreCompany(companyId, icpId)` exige que a Company já esteja persistida, impedindo scoring durante o preview (antes do aceite).

### Evidence

```java
// CompanyScoringService.java
public ScoreDTO scoreCompany(Long companyId, Long icpId) {
    CompanyDTO companyDTO = companyDataService.findCompany(companyId); // ❌ exige persistência
    // ...
}
```

### Impact

- Impossível calcular score no preview sem persistir Company
- Viola requisito "preview sem persistência"
- Força arquitetura errada (persist -> score vs score -> persist)

## Solution

Criar método de scoring "preview" que aceita objetos de domínio diretamente, sem dependência de persistência:

```java
public ScoreDTO scoreCandidate(Company company, ICP icp) {
    // usa ScoringAIService diretamente
    // retorna ScoreDTO sem persistir nada
}
```

### Implementation Steps

1. Adicionar método `scoreCandidate(Company, ICP)` em `CompanyScoringService`
2. Extrair lógica de scoring (sem persistência) para método reutilizável
3. Manter `scoreCompany(companyId, icpId)` para uso em jobs/endpoints existentes
4. Lead search usa `scoreCandidate` (preview)
5. Lead accept usa `scoreCompany` ou persiste score diretamente

### Acceptance Criteria

- [x] Existe `scoreCandidate(Company, ICP)` que NÃO persiste
- [x] Lead search consegue calcular score sem persistir Company
- [x] Scoring AI é invocado corretamente
- [x] Score retornado é válido e saneado (0..100)

## Dependencies

- TD-015 (ICP default) para garantir que sempre há ICP disponível
- TD-017 (ICP completo) para garantir qualidade do scoring

## Notes

Este fix desacopla scoring de persistência e é fundamental para o fluxo "preview -> accept".
