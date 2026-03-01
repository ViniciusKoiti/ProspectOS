# PASSO 4: Testes de Busca de Leads

## Objetivo
Validar funcionalidade principal: busca de leads por diferentes critérios.

## Comandos para Executar

### 4.1 Busca por Fintech
```bash
curl -s -X POST http://localhost:8080/api/leads/search \
  -H "Content-Type: application/json" \
  -d '{"query": "fintech", "limit": 5}' | jq -r '.status, .leads | length'
```
**Esperado:** Status "COMPLETED" e 3-5 resultados

### 4.2 Busca por Tecnologia
```bash
curl -s -X POST http://localhost:8080/api/leads/search \
  -H "Content-Type: application/json" \
  -d '{"query": "tecnologia", "limit": 5}' | jq -r '.leads[0].candidate.name'
```
**Esperado:** Nome de empresa tech como "Nubank" ou "RD Station"

### 4.3 Busca por Agronegócio
```bash
curl -s -X POST http://localhost:8080/api/leads/search \
  -H "Content-Type: application/json" \
  -d '{"query": "agronegócio", "limit": 5}' | jq -r '.leads[].candidate.name'
```
**Esperado:** Empresas como "SLC Agrícola", "Coamo", etc.

### 4.4 Validar Scores dos Leads
```bash
curl -s -X POST http://localhost:8080/api/leads/search \
  -H "Content-Type: application/json" \
  -d '{"query": "startup", "limit": 3}' | jq -r '.leads[].score | "\(.value) - \(.category)"'
```
**Esperado:** Scores entre 45-95 com categorias HOT/WARM/COLD

### 4.5 Validar Source Attribution  
```bash
curl -s -X POST http://localhost:8080/api/leads/search \
  -H "Content-Type: application/json" \
  -d '{"query": "empresa", "limit": 5}' | jq -r '.leads[].source.sourceName' | sort | uniq
```
**Esperado:** Sources como "in-memory", "vector-company"

## Critérios de Sucesso
- [ ] Todas as buscas retornam status "COMPLETED"
- [ ] Fintech query retorna Nubank, Stone, etc.
- [ ] Agro query retorna SLC Agrícola, cooperativas
- [ ] Tech query retorna empresas de tecnologia
- [ ] Scores estão entre 45-95
- [ ] Categorias HOT/WARM/COLD estão presentes
- [ ] Sources são identificadas corretamente

## Cenários de Demo
```bash
# Cenário 1: CTOs de Startups
curl -s -X POST http://localhost:8080/api/leads/search \
  -H "Content-Type: application/json" \
  -d '{"query": "CTO startup tecnologia", "limit": 3}'

# Cenário 2: Diretores de Agronegócio  
curl -s -X POST http://localhost:8080/api/leads/search \
  -H "Content-Type: application/json" \
  -d '{"query": "diretor agronegócio fazenda", "limit": 3}'
```

## Em Caso de Erro
- Verificar se endpoint está respondendo
- Verificar formato JSON da requisição
- Verificar se dados foram populados corretamente

## Próximo Passo
Se busca de leads funciona → **PASSO 5: Teste da Integração CNPJ.ws**