# PASSO 3: Testes dos Endpoints Básicos

## Objetivo
Validar que todos os endpoints REST estão funcionando e retornando dados.

## Comandos para Executar (em novo terminal)

### 3.1 Teste Endpoint de Companies
```bash
curl -s http://localhost:8080/api/companies | jq '.[0:3]'
```
**Esperado:** Lista de empresas com dados como Nubank, Stone, etc.

### 3.2 Teste Endpoint de ICPs
```bash
curl -s http://localhost:8080/api/icps
```
**Esperado:** Lista de 3 ICPs (CTOs Startups, Diretores Agro, Founders Fintech)

### 3.3 Contagem de Dados
```bash
# Contar empresas
curl -s http://localhost:8080/api/companies | jq length

# Contar ICPs  
curl -s http://localhost:8080/api/icps | jq length
```
**Esperado:** 50+ empresas, 3 ICPs

### 3.4 Verificar Estrutura dos Dados
```bash
# Ver estrutura de empresa
curl -s http://localhost:8080/api/companies | jq '.[0]'

# Ver estrutura de ICP
curl -s http://localhost:8080/api/icps | jq '.[0]'
```

## Critérios de Sucesso
- [ ] Endpoint /api/companies retorna 50+ empresas
- [ ] Endpoint /api/icps retorna 3 ICPs
- [ ] Dados incluem empresas brasileiras (Nubank, SLC Agrícola, etc.)
- [ ] Estrutura JSON está correta (id, name, industry, etc.)
- [ ] Sem erros 500 ou timeouts

## Empresas Esperadas nos Dados
- **Fintech:** Nubank, Stone, Creditas
- **Agro:** SLC Agrícola, Coamo, Aegro  
- **Tech:** Magazine Luiza, RD Station, Pipefy
- **Startups:** TechBrasil, InnovaCorp, HealthTech

## Em Caso de Erro
- Verificar se aplicação ainda está rodando
- Verificar se DataSeeder executou corretamente
- Verificar logs do console da aplicação

## Próximo Passo
Se endpoints básicos funcionam → **PASSO 4: Testes de Busca de Leads**