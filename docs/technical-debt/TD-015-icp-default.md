# TD-015: ICP default quando icpId não vier

**Status:** OPEN  
**Priority:** P1 (HIGH - Product)  
**Created:** 2025-01-17  
**Module:** `infrastructure.service.leads`

## Problem

`LeadSearchRequest.icpId` é opcional, mas score é obrigatório. Sem ICP, não é possível calcular score.

### Current Behavior

- Se `icpId` não vem → serviços ignoram e não calculam score (ou quebram)
- Usuário não sabe que precisa passar `icpId`

### Desired Behavior

- Se `icpId` não vem → usar ICP default configurado
- Se não existe default configurado → retornar 400 com mensagem clara

## Solution

Adicionar configuração de ICP default:

```properties
# application.properties
prospectos.leads.default-icp-id=1
```

Lógica no serviço:
```java
Long icpId = request.icpId() != null 
    ? request.icpId() 
    : getDefaultIcpId();

if (icpId == null) {
    throw new IllegalArgumentException(
        "icpId is required. Either pass icpId in request or configure prospectos.leads.default-icp-id"
    );
}
```

### Implementation Steps

1. Criar `LeadSearchProperties` com `defaultIcpId`
2. Injetar em serviços de lead search
3. Implementar fallback: `request.icpId ?? defaultIcpId ?? throw 400`
4. Atualizar `application.properties` com exemplo
5. Documentar no README/AGENTS.md

### Acceptance Criteria

- [x] Property `prospectos.leads.default-icp-id` existe
- [x] Se `icpId` não vem e default existe → usa default
- [x] Se `icpId` não vem e default não existe → 400 com mensagem clara
- [x] Testes validam ambos os cenários

## Notes

Existe property similar para job de scoring (`prospectos.scoring.icp-id`), mas não é usada pelo lead search. Esta nova property é específica para leads.
