# TD-014: CompanyDTO inadequado para preview (tem id)

**Status:** OPEN  
**Priority:** P1 (HIGH - API Model)  
**Created:** 2025-01-17  
**Module:** `api.dto`

## Problem

`LeadResultDTO` usa `CompanyDTO` que tem campo `id`, mas no preview a Company ainda não foi persistida (não existe `id` real).

### Evidence

```java
// LeadResultDTO.java
public record LeadResultDTO(
    CompanyDTO company,  // ❌ CompanyDTO tem 'Long id'
    ScoreDTO score,
    SourceProvenanceDTO source
) {}

// CompanyDTO.java
public record CompanyDTO(
    Long id,  // ❌ não existe no preview
    String name,
    // ...
) {}
```

### Impact

- Semântica confusa: `id` no preview é `null` ou fake
- Cliente não consegue distinguir "candidato" vs "entity persistida"
- Viola princípio de design: DTO deve refletir estado real

## Solution

Criar `CompanyCandidateDTO` (sem `id`) para uso exclusivo no preview:

```java
public record CompanyCandidateDTO(
    String name,
    String website,
    String industry,
    String description,
    String size,
    String location,
    List<String> contacts  // emails brutos
) {}
```

Atualizar `LeadResultDTO`:
```java
public record LeadResultDTO(
    CompanyCandidateDTO candidate,  // ✅ sem id
    ScoreDTO score,
    SourceProvenanceDTO source,
    String leadKey  // ✅ adicionar para accept
) {}
```

### Implementation Steps

1. Criar `CompanyCandidateDTO.java` em `api.dto`
2. Alterar `LeadResultDTO` para usar `candidate` (quebra contrato)
3. Adicionar `leadKey` em `LeadResultDTO`
4. Atualizar serviços de lead search para retornar `CompanyCandidateDTO`
5. Atualizar testes

### Breaking Change

**Antes:**
```json
{
  "company": {
    "id": null,
    "name": "TechCorp",
    // ...
  }
}
```

**Depois:**
```json
{
  "candidate": {
    "name": "TechCorp",
    "website": "https://techcorp.com",
    // ... (sem id)
  },
  "leadKey": "abc123..."
}
```

### Acceptance Criteria

- [x] `CompanyCandidateDTO` existe e não tem `id`
- [x] `LeadResultDTO` usa `candidate` em vez de `company`
- [x] `leadKey` está presente em `LeadResultDTO`
- [x] Preview retorna dados suficientes para aceite posterior

## Alternative (rejected)

Permitir `id = null` em `CompanyDTO` → rejeitado porque:
- Semântica ambígua (null pode significar erro)
- Melhor ter tipos explícitos (candidato vs entity)

## Notes

Este fix melhora clareza do contrato e facilita implementação do accept.
