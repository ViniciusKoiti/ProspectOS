# PASSO 7: Testes de Performance

## Objetivo
Validar que a aplicação atende aos critérios de performance da Semana 1.

## Comandos para Executar

### 7.1 Tempo de Resposta - Busca Simples
```bash
# Medir tempo de resposta
time curl -s -X POST http://localhost:8080/api/leads/search \
  -H "Content-Type: application/json" \
  -d '{"query": "tecnologia", "limit": 10}' > /dev/null
```
**Esperado:** < 5 segundos

### 7.2 Tempo de Resposta - Endpoints Básicos
```bash
# Companies endpoint
time curl -s http://localhost:8080/api/companies > /dev/null

# ICPs endpoint  
time curl -s http://localhost:8080/api/icps > /dev/null
```
**Esperado:** < 2 segundos cada

### 7.3 Teste de Carga - Múltiplas Requisições
```bash
# Executar 5 buscas simultâneas
for i in {1..5}; do
  curl -s -X POST http://localhost:8080/api/leads/search \
    -H "Content-Type: application/json" \
    -d '{"query": "startup'$i'", "limit": 5}' &
done
wait
echo "Todas as requisições completadas"
```

### 7.4 Monitoramento de Memória
```bash
# Ver processos Java
ps aux | grep java | grep prospectos

# No Windows usar:
# tasklist | findstr java
```

### 7.5 Teste com Queries Complexas  
```bash
# Query complexa com múltiplas sources
time curl -s -X POST http://localhost:8080/api/leads/search \
  -H "Content-Type: application/json" \
  -d '{"query": "CTO startup tecnologia agronegócio fintech", "limit": 20}' > /dev/null
```

## Critérios de Performance

### ✅ Metas da Semana 1:
- **Startup time:** < 30 segundos
- **Search time:** < 5 segundos para queries típicas  
- **Memory usage:** < 1GB (stable)
- **Basic endpoints:** < 2 segundos
- **Zero crashes:** durante 1h de uso

### 7.6 Teste de Estabilidade
```bash
# Executar por 5 minutos
echo "Iniciando teste de estabilidade..."
for i in {1..50}; do
  curl -s -X POST http://localhost:8080/api/leads/search \
    -H "Content-Type: application/json" \
    -d '{"query": "test'$i'", "limit": 3}' > /dev/null
  
  if [ $((i % 10)) -eq 0 ]; then
    echo "Completadas $i requisições..."
  fi
  
  sleep 2
done
echo "Teste de estabilidade concluído"
```

## Benchmarks Esperados

### Tempos de Resposta:
- **GET /api/companies:** 200-800ms
- **GET /api/icps:** 100-500ms  
- **POST /api/leads/search (tech):** 1-4s
- **POST /api/leads/search (cnpj-ws):** 1-5s

### Volumes de Dados:
- **50+ companies** retornadas rapidamente
- **3 ICPs** carregados instantaneamente
- **10-20 leads** por busca em < 5s

## Comandos de Monitoring

### 7.7 Verificar Status da Aplicação
```bash
# Health check contínuo
while true; do
  status=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/companies)
  echo "$(date): Status $status"
  if [ "$status" != "200" ]; then
    echo "ERRO: Aplicação não está respondendo!"
    break
  fi
  sleep 10
done
```

## Em Caso de Performance Ruim
- Verificar uso de CPU/memória
- Revisar logs da aplicação
- Verificar se H2 está sobrecarregado  
- Considerar otimizações nas queries

## Próximo Passo
Se performance está boa → **PASSO 8: Cenários de Demo**