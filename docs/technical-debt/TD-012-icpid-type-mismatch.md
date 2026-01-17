# TD-012: LeadSearchRequest.icpId está como UUID (inconsistente)

**Status:** OPEN  
**Priority:** P0 (CRITICAL - Contract)  
**Created:** 2025-01-17  
**Module:** `api.dto`

## Problem

`LeadSearchRequest.icpId` está declarado como `UUID`, mas toda a API de ICP usa `Long` como identificador externo.

### Evidence

- `LeadSearchRequest.java`: `UUID icpId`
- `ICPDto.java`: `Long id`
- `ICPController`: `GET/PUT/DELETE /api/icps/{id}` usa `@PathVariable Long id`
- `CompanyController`: `PUT /api/companies/{id}/score` usa `Long icpId` no body

### Impact

- Quebra de contrato: cliente não consegue usar ICP id obtido da API `/api/icps` diretamente no lead search
- Confusão e bugs silenciosos
- Inconsistência entre módulos

## Solution

Padronizar `icpId` para `Long` em `LeadSearchRequest`.

### Breaking Change

Esta mudança é **breaking** para clientes que já usam o endpoint (se houver).

**Antes:**
```json
{
  "query": "software companies",
  "icpId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Depois:**
```json
{
  "query": "software companies",
  "icpId": 1
}
```

### Implementation Steps

1. Alterar `LeadSearchRequest.java`: `UUID icpId` → `Long icpId`
2. Atualizar serviços de lead search para usar `Long`
3. Atualizar testes (integration + unit)
4. Documentar breaking change no CHANGELOG/release notes

### Acceptance Criteria

- [x] `LeadSearchRequest.icpId` é `Long`
- [x] Serviços aceitam `Long icpId` sem conversões
- [x] Testes passam com novo tipo
- [x] Consistente com resto da API (`ICPDto`, `CompanyController`)

## Alternative (rejected)

Padronizar tudo para `UUID` → rejeitado porque:
- Mudança muito maior (afeta controllers, DTOs, services)
- `Long` é mais simples para IDs externos
- JPA repository já usa UUID internamente (transparente)

## Notes

Este fix é rápido mas importante para consistência de contrato.
