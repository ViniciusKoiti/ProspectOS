# Fluxograma de Uso do Aplicativo (Estado Atual)

Data de referência: 2026-03-14

## 1) Jornada principal do usuário

```mermaid
flowchart TD
    A[Usuário abre ProspectOS Web] --> B[MainLayout + Navegação]
    B --> C[Dashboard /]
    B --> D[Busca /search]
    B --> E[ICPs /icps]
    B --> F[Empresas /companies]

    C --> C1[GET /api/companies]
    C --> C2[GET /api/icps]
    C1 --> C3[Cards de métricas]
    C2 --> C3

    D --> D1[GET /api/icps]
    D1 --> D2[Preenche query + limit + icp opcional]
    D2 --> D3[POST /api/leads/search]
    D3 --> D4{Resultado da busca}
    D4 -->|Erro| D5[ErrorState + Retry]
    D4 -->|Sucesso| D6[DataTable de leads]
    D6 --> D7[Aceitar lead]
    D7 --> D8[POST /api/leads/accept]
    D8 --> D9[Invalida cache companies]
    D9 --> D10[Link para /companies/:id]

    E --> E1[GET /api/icps]
    E1 --> E2{Ação}
    E2 -->|Criar| E3[POST /api/icps]
    E2 -->|Editar| E4[PUT /api/icps/:id]
    E2 -->|Excluir| E5[DELETE /api/icps/:id]
    E3 --> E6[Refetch lista]
    E4 --> E6
    E5 --> E6

    F --> F1[GET /api/companies]
    F1 --> F2[Tabela de empresas]
    F2 --> F3[Abrir detalhe /companies/:id]
    F3 --> F4[GET /api/companies/:id]
```

## 2) Fluxo detalhado de busca e aceite

```mermaid
sequenceDiagram
    participant U as Usuário
    participant FE as Frontend (SearchPage)
    participant API as Backend API

    U->>FE: Abre /search
    FE->>API: GET /api/icps
    API-->>FE: Lista de ICPs

    U->>FE: Envia formulário de busca
    FE->>API: POST /api/leads/search
    API-->>FE: LeadSearchResponse

    alt status erro/falha
        FE-->>U: Exibe ErrorState + Retry
    else status completed
        FE-->>U: Exibe tabela de leads
        U->>FE: Clica em "Aceitar"
        FE->>API: POST /api/leads/accept
        API-->>FE: AcceptLeadResponse (company)
        FE->>FE: invalidateQueries(['companies'])
        FE-->>U: Link para detalhe da empresa criada/atualizada
    end
```

## Observações práticas do estado atual

- IDs de `Company` e `ICP` no frontend são tratados como string para evitar problemas de precisão no JavaScript.
- Na camada backend, o `externalId` foi ajustado para política de faixa segura JS e normalização em `development`.
- Em caso de falha de carregamento ou mutação, as páginas usam `ErrorState` com ação de tentativa (`Retry`) quando aplicável.
