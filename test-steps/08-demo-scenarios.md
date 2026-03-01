# PASSO 8: Cenários de Demo

## Objetivo
Executar cenários reais de demonstração que mostram o valor do sistema.

## 🎪 CENÁRIO 1: Buscando CTOs de Startups Tech

### Query Realística:
```bash
curl -s -X POST http://localhost:8080/api/leads/search \
  -H "Content-Type: application/json" \
  -d '{"query": "CTO startup tecnologia São Paulo", "limit": 5}' | jq -r '.leads[] | "🏢 \(.candidate.name) | 📍 \(.candidate.location) | ⭐ Score: \(.score.value)"'
```

### Resultado Esperado:
```
🏢 TechSolutions Brasil Ltda | 📍 São Paulo, SP - Brazil | ⭐ Score: 85
🏢 CloudBrasil Sistemas | 📍 Florianópolis, SC - Brazil | ⭐ Score: 82
🏢 InovaPay Fintech SA | 📍 Rio de Janeiro, RJ - Brazil | ⭐ Score: 88
```

## 🌾 CENÁRIO 2: Diretores do Agronegócio

### Query Realística:
```bash
curl -s -X POST http://localhost:8080/api/leads/search \
  -H "Content-Type: application/json" \
  -d '{"query": "diretor agronegócio fazenda Mato Grosso", "limit": 5}' | jq -r '.leads[] | "🌾 \(.candidate.name) | 🏭 \(.candidate.industry) | 📊 \(.score.category)"'
```

### Resultado Esperado:
```
🌾 SLC Agrícola | 🏭 agribusiness | 📊 HOT
🌾 AgroTech Mato Grosso Ltda | 🏭 agtech | 📊 WARM  
🌾 FarmData Analytics | 🏭 agtech | 📊 HOT
```

## 💳 CENÁRIO 3: Founders de Fintech

### Query Realística:
```bash
curl -s -X POST http://localhost:8080/api/leads/search \
  -H "Content-Type: application/json" \
  -d '{"query": "founder fintech pagamento digital", "limit": 3}' | jq -r '.leads[] | "💳 \(.candidate.name) | 💰 \(.candidate.description) | 🎯 \(.score.reasoning)"'
```

## 📊 CENÁRIO 4: Comparação de Sources

### Multiple Sources Test:
```bash
echo "=== RESULTADOS IN-MEMORY ==="
curl -s -X POST http://localhost:8080/api/leads/search \
  -H "Content-Type: application/json" \
  -d '{"query": "tecnologia", "sources": ["in-memory"], "limit": 3}' | jq -r '.leads[].candidate.name'

echo "=== RESULTADOS CNPJ.WS ==="
curl -s -X POST http://localhost:8080/api/leads/search \
  -H "Content-Type: application/json" \
  -d '{"query": "tecnologia", "sources": ["cnpj-ws"], "limit": 3}' | jq -r '.leads[].candidate.name'
```

## 🎯 CENÁRIO 5: Demo Dashboard - Métricas

### Estatísticas Gerais:
```bash
echo "📊 DASHBOARD DE MÉTRICAS"
echo "========================"

# Total de empresas por indústria
echo "🏭 EMPRESAS POR SETOR:"
curl -s http://localhost:8080/api/companies | jq -r '.[] | .industry' | sort | uniq -c | sort -nr

# Distribuição de scores
echo "⭐ DISTRIBUIÇÃO DE SCORES:"
curl -s -X POST http://localhost:8080/api/leads/search \
  -H "Content-Type: application/json" \
  -d '{"query": "empresa", "limit": 20}' | \
  jq -r '.leads[].score.category' | sort | uniq -c

# Empresas por localização (Brasil)
echo "📍 EMPRESAS BRASILEIRAS:"
curl -s http://localhost:8080/api/companies | jq -r '.[] | select(.location | contains("Brazil")) | .location' | sort | uniq -c
```

## 🎬 CENÁRIO 6: Pitch de Vendas

### Demo Script para Cliente:
```bash
echo "🎬 DEMO PARA CLIENTE: Prospectando Fintechs"
echo "=========================================="

echo "1️⃣ Buscando fintechs em crescimento..."
curl -s -X POST http://localhost:8080/api/leads/search \
  -H "Content-Type: application/json" \
  -d '{"query": "fintech pagamento digital startup", "limit": 3}' | \
  jq -r '.leads[] | "✨ \(.candidate.name) - Score \(.score.value) (\(.score.category)) - \(.candidate.location)"'

echo "2️⃣ Validando dados via CNPJ.ws..."
curl -s -X POST http://localhost:8080/api/leads/search \
  -H "Content-Type: application/json" \
  -d '{"query": "fintech", "sources": ["cnpj-ws"], "limit": 2}' | \
  jq -r '.leads[] | "✅ \(.candidate.name) - Validado via CNPJ"'

echo "3️⃣ Analisando potencial..."
curl -s -X POST http://localhost:8080/api/leads/search \
  -H "Content-Type: application/json" \
  -d '{"query": "fintech", "limit": 1}' | \
  jq -r '.leads[0].score.reasoning'
```

## 🔍 CENÁRIO 7: Busca Avançada

### Query Complexa Multi-Critério:
```bash
curl -s -X POST http://localhost:8080/api/leads/search \
  -H "Content-Type: application/json" \
  -d '{
    "query": "startup tecnologia agricultura sustentabilidade", 
    "limit": 10
  }' | jq -r '.leads[] | select(.score.value > 80) | "🚀 \(.candidate.name) (\(.candidate.industry)) - Score: \(.score.value)"'
```

## Critérios de Sucesso do Demo

### ✅ Cada cenário deve demonstrar:
- [ ] Busca retorna resultados relevantes
- [ ] Scores fazem sentido (startups = score alto)
- [ ] Localização correta (empresas brasileiras)
- [ ] Sources funcionam independentemente
- [ ] Reasoning é compreensível
- [ ] Performance é aceitável (< 5s)

### 📈 KPIs de Demo:
- **Relevância:** 80%+ dos resultados são pertinentes
- **Velocidade:** Busca em < 5 segundos
- **Cobertura:** 3 sources diferentes funcionando
- **Qualidade:** Scores variam realisticamente (45-95)

## Em Caso de Resultado Ruim
- Verificar se dados foram carregados corretamente
- Ajustar queries para termos mais específicos  
- Verificar configuração de sources
- Revisar algoritmo de scoring se necessário

## Próximo Passo
Se cenários de demo passam → **PASSO FINAL: Relatório de Validação**