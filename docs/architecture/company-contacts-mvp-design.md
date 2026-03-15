# Company Contacts MVP - Desenho Técnico

Data: 2026-03-14
Status: proposta para implementação incremental

## 1. Objetivo

Evoluir o produto de "lista de empresas" para "lista de empresas com pessoas acionáveis", permitindo que o time comercial saiba **quem** abordar em cada conta.

## 2. Problema atual

- A aplicação lista empresas, mas não expõe contatos na UI de empresas.
- O domínio já suporta contatos (`CompanyRelationshipState`), mas o contrato de API (`CompanyDTO`) não os inclui.
- No fluxo `acceptLead`, os contatos do candidato são recebidos como lista de strings, porém não entram de forma estruturada na entidade da empresa.

## 3. Escopo MVP (Fase 1)

### Incluído

- Expor contatos da empresa via API dedicada.
- Persistir contatos durante `acceptLead` (quando vierem no payload do candidato).
- Expor no resumo da empresa campos para operação comercial:
  - `primaryContactEmail`
  - `contactCount`
- Mostrar `primaryContactEmail` e `contactCount` no frontend de empresas.

### Fora de escopo (Fase 2+)

- Enriquecimento avançado de pessoa (LinkedIn, senioridade, cargo inferido por IA).
- Workflow de outreach (cadência, histórico de tentativa, bounce tracking).
- Modelo separado `Person` com identidade cross-company.

## 4. Modelo de dados (alvo do MVP)

O domínio atual já possui `Contact` embutido em `Company` com:

- `name`
- `email`
- `position`
- `phoneNumber`

Para o MVP, manteremos `Contact` como value object dentro de `Company`.

## 5. Estratégia de persistência no acceptLead

Fluxo atual recebe `candidate.contacts: List<String>`.

Proposta MVP:

- Para cada email válido em `candidate.contacts`, adicionar contato na empresa.
- Nome padrão: parte local do email normalizada (fallback simples).
- Cargo/telefone: `null` (até termos enriquecimento de pessoa).
- Deduplicação: regra de unicidade por email da própria entidade `Company`.

## 6. Contratos de leitura para frontend

### Resumo da empresa (`CompanyDTO`)

Adicionar:

- `primaryContactEmail: String?`
- `contactCount: Integer`

Regra de cálculo:

- `primaryContactEmail`: primeiro email válido da coleção de contatos (ordem de inserção).
- `contactCount`: total de contatos da empresa.

### Lista detalhada de contatos

Novo endpoint para detalhe operacional:

- `GET /api/companies/{companyId}/contacts`

Resposta: lista de `CompanyContactDTO`.

## 7. UI alvo (incremental)

### `/companies`

- Nova coluna: `Contato principal`
- Badge ou texto com `contactCount`

### `/companies/:id`

- Nova seção "Pessoas" com tabela simples:
  - Nome
  - Cargo
  - Email
  - Telefone

## 8. Regras de negócio

- Não persistir emails inválidos.
- Não duplicar contato com mesmo email na mesma empresa.
- Não remover contatos existentes no `acceptLead`; apenas adicionar novos.
- Nunca sobrescrever contatos manualmente cadastrados sem ação explícita de update.

## 9. Testes mínimos

- Unit:
  - mapper de `Contact -> CompanyContactDTO`
  - cálculo de `primaryContactEmail` e `contactCount`
- Service:
  - `acceptLead` persiste contatos válidos e ignora inválidos/duplicados
- Integration:
  - `GET /api/companies/{id}/contacts` retorna contatos persistidos
  - lista de empresas expõe `primaryContactEmail` e `contactCount`
- Frontend:
  - contrato de `CompanyDTO` atualizado
  - renderização da coluna de contato sem quebrar estados `loading/error/empty`

## 10. Riscos e mitigação

- Risco: regressão no fluxo atual de aceitação de lead.
  - Mitigação: testes de regressão no `LeadAcceptService`.
- Risco: baixa qualidade de emails da fonte.
  - Mitigação: filtro e validação antes de persistir.
- Risco: sobrecarga visual na listagem.
  - Mitigação: exibir apenas contato principal + contador; detalhes ficam na tela de empresa.

## 11. Plano de implementação sugerido

1. Backend contratos (`CompanyDTO` + `CompanyContactDTO` + endpoint de contatos).
2. Backend aplicação (`LeadAcceptService` persistindo contatos).
3. Testes backend (unit/integration).
4. Frontend contratos e telas (`companies` e `company detail`).
5. Testes frontend de contrato.
