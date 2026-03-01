# 🚀 TESTE RÁPIDO - MVP SEMANA 1

## Como Executar os Testes (COM CALMA!)

### **OPÇÃO 1: Teste Manual Passo-a-Passo** ⭐ RECOMENDADO

1. **Iniciar aplicação:**
   ```bash
   ./gradlew bootRun --args="--spring.profiles.active=mock"
   ```
   
2. **Aguardar mensagem:** `Started ProspectosApplication in X.X seconds`

3. **Testar endpoints básicos** (em novo terminal):
   ```bash
   # Teste 1: Empresas
   curl http://localhost:8080/api/companies
   
   # Teste 2: ICPs  
   curl http://localhost:8080/api/icps
   
   # Teste 3: Busca tech
   curl -X POST http://localhost:8080/api/leads/search \
     -H "Content-Type: application/json" \
     -d '{"query": "tecnologia", "limit": 5}'
   ```

4. **Validar H2 Console:**
   - Abrir: http://localhost:8080/h2-console
   - URL: `jdbc:h2:mem:prospectos`
   - User: `sa`, Password: (vazio)

### **OPÇÃO 2: Script Automático PowerShell**

```powershell
# Executar script completo
.\run-all-tests.ps1 -Full

# Ou apenas testes básicos
.\run-all-tests.ps1 -Quick

# Ou cenários de demo
.\run-all-tests.ps1 -Demo
```

### **OPÇÃO 3: Seguir Passos Individuais**

Execute na ordem:
1. `test-steps/01-environment-check.md`
2. `test-steps/02-application-startup.md`
3. `test-steps/03-basic-endpoints.md`
4. `test-steps/04-lead-search.md`
5. `test-steps/05-cnpj-integration.md`
6. `test-steps/06-h2-console.md`
7. `test-steps/07-performance.md`
8. `test-steps/08-demo-scenarios.md`

---

## 🎯 Cenários de Demo Principais

### **Cenário 1: CTOs de Startups**
```bash
curl -s -X POST http://localhost:8080/api/leads/search \
  -H "Content-Type: application/json" \
  -d '{"query": "CTO startup tecnologia", "limit": 5}'
```
**Deve retornar:** Empresas como Nubank, RD Station, startups tech

### **Cenário 2: Agronegócio**  
```bash
curl -s -X POST http://localhost:8080/api/leads/search \
  -H "Content-Type: application/json" \
  -d '{"query": "agronegócio fazenda", "limit": 5}'
```
**Deve retornar:** SLC Agrícola, Coamo, empresas do agro

### **Cenário 3: CNPJ.ws Integration**
```bash
curl -s -X POST http://localhost:8080/api/leads/search \
  -H "Content-Type: application/json" \
  -d '{"query": "tecnologia SP", "sources": ["cnpj-ws"], "limit": 3}'
```
**Deve retornar:** Empresas com source "cnpj-ws"

---

## ✅ Critérios de Sucesso

### **Funcional:**
- [ ] Aplicação inicia sem erros (< 30s)
- [ ] 50+ empresas no database
- [ ] 3 ICPs configurados  
- [ ] Buscas retornam resultados relevantes
- [ ] CNPJ source funciona
- [ ] H2 console acessível

### **Performance:**
- [ ] Startup < 30s
- [ ] Search < 5s
- [ ] Memory < 1GB
- [ ] Zero crashes

### **Demo Ready:**
- [ ] Query "fintech" → Nubank, Stone
- [ ] Query "agro" → SLC Agrícola, Coamo  
- [ ] Query "tech startup" → múltiplos resultados
- [ ] Scores variam 45-95
- [ ] Sources identificadas

---

## 🚨 Em Caso de Problema

### **Erro de Startup:**
1. Verificar Java 21: `java -version`
2. Limpar build: `./gradlew clean`
3. Rebuild: `./gradlew build -x test`

### **Sem Dados:**
1. Verificar logs do DataSeeder
2. Confirmar profile "mock" ativo
3. Acessar H2 console para debug

### **Performance Ruim:**
1. Verificar uso de CPU/RAM
2. Restart da aplicação
3. Testar queries mais específicas

---

## 🎪 Quick Demo Commands

```bash
# Quick health check
curl http://localhost:8080/api/companies | jq length

# Demo search  
curl -X POST http://localhost:8080/api/leads/search \
  -H "Content-Type: application/json" \
  -d '{"query": "fintech", "limit": 3}' | jq '.leads[].candidate.name'

# Performance test
time curl -X POST http://localhost:8080/api/leads/search \
  -H "Content-Type: application/json" \
  -d '{"query": "startup", "limit": 10}' > /dev/null
```

## 📊 Expected Results

**Empresas esperadas nos resultados:**
- **Fintech:** Nubank, Stone Pagamentos, Creditas
- **Agro:** SLC Agrícola, Coamo, Aegro
- **Tech:** Magazine Luiza, RD Station, Pipefy
- **Startups:** TechBrasil, InnovaCorp, HealthTech

**Sources esperadas:**
- `in-memory` (dados seeded)
- `vector-company` (busca semântica)  
- `cnpj-ws` (integração externa)

**Score distribution:**
- HOT (>=80): ~30% dos resultados
- WARM (65-79): ~50% dos resultados
- COLD (<65): ~20% dos resultados