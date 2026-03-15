# Company Contacts API Contract (MVP)

Data: 2026-03-14
Status: draft

## 1. Company summary contract

Endpoint existente:

- `GET /api/companies`
- `GET /api/companies/{companyId}`

### Response (CompanyDTO)

```json
{
  "id": "12345",
  "name": "Acme Ltd",
  "industry": "Software",
  "website": "https://acme.com",
  "description": "B2B SaaS",
  "employeeCount": 120,
  "location": "Sao Paulo, BR",
  "score": {
    "value": 82,
    "category": "HOT",
    "reasoning": "Good fit"
  },
  "primaryContactEmail": "ceo@acme.com",
  "contactCount": 3
}
```

## 2. Company contacts listing

Novo endpoint:

- `GET /api/companies/{companyId}/contacts`

### Path params

- `companyId` (required): id externo da empresa

### Response 200

```json
[
  {
    "name": "Maria Silva",
    "email": "maria@acme.com",
    "position": "CTO",
    "phoneNumber": "+55 11 99999-0000"
  },
  {
    "name": "Joao Lima",
    "email": "joao@acme.com",
    "position": null,
    "phoneNumber": null
  }
]
```

### Response 404

```json
{
  "error": "Company not found"
}
```

## 3. Lead accept (input unchanged, behavior evolved)

Endpoint existente:

- `POST /api/leads/accept`

### Request (trecho relevante)

```json
{
  "leadKey": "...",
  "candidate": {
    "name": "Acme Ltd",
    "website": "https://acme.com",
    "contacts": [
      "ceo@acme.com",
      "sales@acme.com"
    ]
  },
  "score": { "value": 80, "category": "HOT", "reasoning": "..." },
  "source": { "sourceName": "in-memory", "sourceUrl": "https://acme.com", "collectedAt": "2026-03-14T18:00:00Z" }
}
```

### Novo comportamento esperado

- Persistir contatos válidos em `Company.contacts`.
- Ignorar contatos inválidos.
- Não duplicar emails já existentes para a mesma empresa.

### Response 200

Mantém contrato atual de `AcceptLeadResponse` (com `company` atualizado, incluindo `primaryContactEmail` e `contactCount` no `CompanyDTO`).

## 4. Validações

- `email` deve ser válido para persistência.
- `companyId` inválido ou inexistente retorna erro adequado (`400`/`404`).
- Contratos devem permanecer compatíveis com frontend (`id` textual no web).

## 5. Compatibilidade

- Mudança é backward-compatible para consumidores que ignoram novos campos.
- Frontend novo passa a usar `primaryContactEmail` e `contactCount` quando disponíveis.
