# MCP Integration Guide - ProspectOS

## O que é MCP?

MCP (Model Context Protocol) permite que agentes de IA autônomos interajam com o ProspectOS através de ferramentas padronizadas, otimizando automaticamente o sistema de prospecção baseado em dados reais.

## Principais Benefícios

### 🔄 Otimização Automática
- IA monitora custos de APIs em tempo real
- Troca automaticamente para providers mais baratos quando necessário
- Mantém qualidade dos resultados dentro de thresholds definidos

### 📊 Monitoramento Inteligente  
- Acompanha métricas de performance continuamente
- Detecta providers com problemas ou lentidão
- Redireciona tráfego automaticamente para manter SLA

### 🌍 Prospecção Internacional Melhorada
- Adapta estratégias baseadas no mercado-alvo
- Combina múltiplos providers para melhor cobertura
- Enriquece leads automaticamente com dados de qualidade

## Ferramentas MCP Implementadas

### 1. Query Metrics (`get_query_metrics`)
**Função**: Retorna métricas de performance das consultas
```json
Entrada: {"timeWindow": "1h"}
Saída: {
  "totalQueries": 450,
  "totalCost": 45.60, 
  "successRate": 0.87,
  "avgResponseTime": 2300
}
```

### 2. Provider Routing (`update_provider_routing`)
**Função**: Altera estratégia de roteamento entre providers
```json
Entrada: {
  "strategy": "COST_OPTIMIZED",
  "providerPriority": ["nominatim", "bing-maps", "google-places"]
}
Saída: {"configurationApplied": true}
```

### 3. Provider Health (`get_provider_health`)
**Função**: Verifica status de todos os providers
```json
Saída: {
  "google-places": {"status": "healthy", "lastError": null},
  "bing-maps": {"status": "degraded", "lastError": "rate_limit"},
  "nominatim": {"status": "healthy", "lastError": null}
}
```

### 4. International Search (`search_international_leads`)
**Função**: Busca leads internacionais com orçamento controlado
```json
Entrada: {
  "query": "dentistas em Dallas",
  "budget": 50.00,
  "qualityThreshold": 0.85
}
Saída: {
  "leads": [...],
  "totalCost": 23.40,
  "avgQualityScore": 0.88
}
```

## Como Usar

### 1. Habilitar MCP
```bash
# Iniciar aplicação com MCP
./gradlew bootRun --args="--spring.profiles.active=mcp"
```

### 2. Verificar Funcionamento
```bash
# Listar ferramentas disponíveis
curl http://localhost:8082/mcp/tools/list

# Verificar saúde do MCP
curl http://localhost:8082/actuator/health/mcp
```

### 3. Exemplo de Uso Prático
```bash
# 1. Verificar métricas atuais
curl -X POST http://localhost:8082/mcp/tools/call \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":"1","method":"tools/call","params":{"name":"get_query_metrics","arguments":{"timeWindow":"1h"}}}'

# 2. Se custos estão altos, otimizar para custo
curl -X POST http://localhost:8082/mcp/tools/call \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":"2","method":"tools/call","params":{"name":"update_provider_routing","arguments":{"strategy":"COST_OPTIMIZED"}}}'
```

## Workflows Típicos

### Workflow 1: Otimização de Custo Automática
1. **IA monitora** métricas a cada 5 minutos
2. **Detecta** custo/query > $0.08 
3. **Analisa** breakdown por provider
4. **Implementa** estratégia COST_OPTIMIZED
5. **Verifica** que qualidade se mantém > 80%
6. **Rollback** se qualidade cair muito

### Workflow 2: Recovery de Provider com Problema
1. **IA detecta** queda na success rate
2. **Verifica** health de cada provider
3. **Identifica** provider com problemas
4. **Remove** provider problemático da rotação
5. **Monitora** recovery do provider
6. **Reintegra** quando voltou ao normal

### Workflow 3: Busca Inteligente por Mercado
1. **Usuario solicita** "dentistas no Texas"
2. **IA acessa** dados de mercado do Texas
3. **Detecta** que cidades pequenas têm melhor cobertura no Bing
4. **Ajusta** estratégia para priorizar Bing Maps
5. **Executa** busca otimizada
6. **Enriquece** leads automaticamente

## Transports Suportados

### STDIO (Desenvolvimento)
Ideal para desenvolvimento e testes locais:
```bash
echo '{"jsonrpc":"2.0","method":"tools/list","id":"1"}' | java -jar prospectos.jar --spring.profiles.active=mcp
```

### HTTP (Produção)
Para integração com clientes remotos:
```bash
curl -X POST http://localhost:8082/mcp/tools/call \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","method":"tools/call","params":{...}}'
```

## Configuração

### Básica (application-mcp.yml)
```yaml
spring:
  ai:
    mcp:
      server:
        enabled: true
        stdio: true
        http:
          enabled: true
          port: 8082
```

### Avançada (Com Security)
```yaml
prospectos:
  mcp:
    security:
      enabled: true
      allowed-origins: ["https://claude.ai", "http://localhost:*"]
    tools:
      rate-limit:
        requests-per-minute: 60
```

## Segurança

### Rate Limiting
- Máximo 60 requests/minuto por cliente
- Burst de até 10 requests simultâneos
- Throttling automático se limites excedidos

### Authorization
- Header X-MCP-Auth obrigatório em produção
- Validação de origem para requests HTTP
- Audit log de todas as operações MCP

### Isolation
- MCP tools não podem acessar dados sensíveis diretamente
- Todas as operações passam por layer de validação
- Circuit breakers protegem APIs externas

## Troubleshooting

### MCP Server não inicia
```bash
# Verificar se dependency está presente
./gradlew dependencies | grep spring-ai-starter-mcp-server

# Verificar logs de erro
./gradlew bootRun --args="--spring.profiles.active=mcp" | grep ERROR
```

### Tools não são descobertos
```bash
# Verificar se @Component está presente nas classes
# Verificar se package está sendo escaneado
# Verificar logs de registro das tools
```

### Performance ruim
```bash
# Ativar debug logging
logging.level.org.springframework.ai.mcp: DEBUG

# Verificar métricas de cada tool
curl http://localhost:8082/actuator/metrics/mcp.tool.execution.time
```

---

**Próximo**: Configure o MCP seguindo o [setup detalhado](./mcp-setup.md) ou veja [exemplos práticos](./mcp-examples.md).