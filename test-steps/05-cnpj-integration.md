# PASSO 5: Teste da Integração CNPJ.ws

## Objetivo
Validar que a fonte externa CNPJ.ws está integrada e funcionando.

## Comandos para Executar

### 5.1 Teste Source CNPJ Específica
```bash
curl -s -X POST http://localhost:8080/api/leads/search \
  -H "Content-Type: application/json" \
  -d '{"query": "tecnologia SP", "sources": ["cnpj-ws"], "limit": 3}' | jq -r '.status'
```
**Esperado:** Status "COMPLETED"

### 5.2 Verificar Empresas CNPJ
```bash
curl -s -X POST http://localhost:8080/api/leads/search \
  -H "Content-Type: application/json" \
  -d '{"query": "empresas brasileiras", "sources": ["cnpj-ws"], "limit": 5}' | jq -r '.leads[].candidate.name'
```
**Esperado:** Empresas como "TechSolutions Brasil", "InovaPay Fintech"

### 5.3 Validar Source Attribution CNPJ
```bash
curl -s -X POST http://localhost:8080/api/leads/search \
  -H "Content-Type: application/json" \
  -d '{"query": "fintech", "sources": ["cnpj-ws"], "limit": 2}' | jq -r '.leads[].source.sourceName'
```
**Esperado:** Todas as respostas devem ser "cnpj-ws"

### 5.4 Teste Multiple Sources
```bash
curl -s -X POST http://localhost:8080/api/leads/search \
  -H "Content-Type: application/json" \
  -d '{"query": "tecnologia", "sources": ["in-memory", "cnpj-ws"], "limit": 10}' | jq -r '.leads[].source.sourceName' | sort | uniq -c
```
**Esperado:** Mistura de "in-memory" e "cnpj-ws"

### 5.5 Verificar Sources Permitidas
```bash
curl -s -X POST http://localhost:8080/api/leads/search \
  -H "Content-Type: application/json" \
  -d '{"query": "empresa", "limit": 5}' | jq -r '.leads[].source.sourceName' | sort | uniq
```
**Esperado:** Lista: "cnpj-ws", "in-memory", "vector-company"

## Mock Data CNPJ Esperado
O CNPJ source deve retornar empresas como:
- **TechSolutions Brasil Ltda** - São Paulo, SP
- **InovaPay Fintech SA** - Rio de Janeiro, RJ  
- **CloudBrasil Sistemas** - Florianópolis, SC
- **AgroTech Mato Grosso** - Cuiabá, MT
- **Consultoria Estratégica Brasil** - Brasília, DF

## Critérios de Sucesso
- [ ] Source "cnpj-ws" está disponível
- [ ] Busca específica por CNPJ source funciona
- [ ] Mock companies brasileiras são retornadas
- [ ] Source attribution está correta
- [ ] Multiple sources funcionam juntas
- [ ] Todas as sources estão na lista permitida

## Queries de Teste Específicas
```bash
# Agro companies via CNPJ
curl -s -X POST http://localhost:8080/api/leads/search \
  -H "Content-Type: application/json" \
  -d '{"query": "agricultura", "sources": ["cnpj-ws"], "limit": 3}'

# Health companies via CNPJ
curl -s -X POST http://localhost:8080/api/leads/search \
  -H "Content-Type: application/json" \
  -d '{"query": "saúde", "sources": ["cnpj-ws"], "limit": 2}'
```

## Em Caso de Erro
- Verificar configuração: `prospectos.sources.cnpj.enabled=true`
- Verificar logs para mensagens de CNPJ source
- Verificar se source está na lista permitida

## Próximo Passo
Se CNPJ integration funciona → **PASSO 6: Validação do H2 Console**