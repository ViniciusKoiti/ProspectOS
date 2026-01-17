# TD-011: Falta endpoint de aceite de lead

**Status:** OPEN  
**Priority:** P0 (CRITICAL - Product)  
**Created:** 2025-01-17  
**Module:** `infrastructure.api.leads`

## Problem

Não existe endpoint ou fluxo para o usuário "aceitar" um lead retornado pelo search. O requisito de negócio é: "persistir Company + score + proveniência + contatos apenas quando o usuário aceitar".

### Business Impact

- Funcionalidade central do produto não existe
- Impossível cumprir requisito "persistir só no aceite"
- Não há controle sobre quais leads entram no pipeline

## Solution

Criar endpoint `POST /api/leads/accept` que:
- Recebe payload completo do lead (stateless)
- Valida `leadKey` para idempotência
- Persiste:
  - Company (com website normalizado)
  - Contatos validados (via `EmailFilter` + `ContactProcessor`)
  - Score (saneado: clamp 0..100, normalize category)
  - SourceProvenance

### Contract

**Request:**
```json
{
  "leadKey": "base64url(sha256(domain|sourceName))",
  "company": {
    "name": "TechCorp",
    "website": "https://techcorp.com",
    "industry": "Software",
    "description": "...",
    "size": "MEDIUM",
    "location": {
      "country": "BR",
      "city": "São Paulo"
    }
  },
  "contacts": [
    "john@techcorp.com",
    "info@techcorp.com"
  ],
  "score": {
    "value": 85,
    "category": "HOT",
    "reasoning": "Strong fit with ICP..."
  },
  "source": {
    "sourceName": "scraper",
    "sourceUrl": "https://techcorp.com",
    "collectedAt": "2025-01-17T10:00:00Z"
  },
  "icpIdUsed": 1
}
```

**Response (200):**
```json
{
  "companyId": 123,
  "status": "ACCEPTED",
  "message": "Lead accepted successfully"
}
```

**Response (400 - leadKey inválido):**
```json
{
  "error": "Invalid leadKey: does not match company data"
}
```

**Response (409 - já existe):**
```json
{
  "error": "Company with website already exists",
  "existingCompanyId": 456
}
```

### Implementation Steps

1. Criar DTOs: `AcceptLeadRequest`, `AcceptLeadResponse`
2. Criar `LeadAcceptService` com lógica:
   - Validar `leadKey` (recalcular e comparar)
   - Sanear `score` (clamp, normalize category)
   - Criar `Company` via domain
   - Processar contatos (via `EmailFilter` + `ContactProcessor`)
   - Persistir Company + score + contatos + proveniência
3. Criar `LeadAcceptController` com endpoint `POST /api/leads/accept`
4. Adicionar testes de integração

### LeadKey Calculation

```java
String domain = Website.of(company.website).getDomain();
String source = sourceName.trim().toLowerCase();
String base = domain + "|" + source;
byte[] hash = sha256(base);
String leadKey = base64url(hash);
```

### Score Sanitization Rules

- `value`: clamp(0, 100)
- `category`: normalize + whitelist (`HOT|WARM|COLD|IGNORE`), fallback: `COLD`
- `reasoning`: limit 2000 chars, fallback: "Accepted lead"

### Idempotency Strategy

- Se `leadKey` já foi aceito (existe Company com mesmo domain) -> decidir:
  1. **Merge/Update** (recomendado): atualizar contatos/score se melhorou
  2. **Reject 409**: retornar erro "already exists"

**Decisão atual:** merge/update (mais flexível para usuário)

### Acceptance Criteria

- [x] Endpoint aceita payload válido e retorna 200 + companyId
- [x] leadKey inválido retorna 400
- [x] Score é saneado antes de persistir
- [x] Contatos são validados e persistidos via `ContactProcessor`
- [x] SourceProvenance é registrada
- [x] Idempotência funciona (aceitar mesmo lead 2x não duplica)

## Dependencies

- TD-010 (remover persistência do search) deve ser feito em paralelo
- TD-013 (scoring preview) para garantir consistência de score
- TD-014 (DTO candidato) facilita design do contrato

## Notes

Este TD é **bloqueador** para o valor do produto. É a funcionalidade central do fluxo de prospecção.
