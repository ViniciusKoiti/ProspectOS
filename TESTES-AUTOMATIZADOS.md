# 🧪 Testes Automatizados - MVP Week 1

## 🎯 O que foi Criado

Criei um **sistema completo de testes automatizados** que valida toda a funcionalidade da Semana 1:

### **📁 Estrutura de Testes:**

```
src/test/java/
├── dev/prospectos/infrastructure/config/
│   └── DataSeederTest.java                    # 🔬 Teste unitário do DataSeeder
├── dev/prospectos/infrastructure/service/discovery/
│   └── CNPJLeadDiscoverySourceTest.java      # 🔬 Teste unitário CNPJ integration  
├── dev/prospectos/integration/
│   └── Week1MVPIntegrationTest.java          # 🔗 Teste de integração completo
└── dev/prospectos/
    └── Week1TestSuite.java                   # 🎯 Suite completa de testes
```

## ⚡ Como Executar

### **OPÇÃO 1: Testes Individuais**
```bash
# Teste do DataSeeder
./gradlew test --tests "*DataSeederTest"

# Teste da integração CNPJ
./gradlew test --tests "*CNPJLeadDiscoverySourceTest"

# Teste de integração completo
./gradlew test --tests "*Week1MVPIntegrationTest"
```

### **OPÇÃO 2: Suite Completa**
```bash
# Todos os testes da Week 1
./gradlew test --tests "*Week1TestSuite"

# Ou todos os testes do projeto
./gradlew test
```

### **OPÇÃO 3: Script Automatizado**
```bash
# Execução completa com relatórios
./run-automated-tests.sh
```

## 🔬 Testes Unitários

### **DataSeederTest** - Valida que:
- ✅ Cria 3 ICPs corretamente (Tech, Agro, Fintech)
- ✅ Cria 30+ empresas diversas
- ✅ Atribui scores realistas (45-95)  
- ✅ Inclui empresas brasileiras específicas (Nubank, SLC)
- ✅ Funciona com mocks (sem dependências externas)

### **CNPJLeadDiscoverySourceTest** - Valida que:
- ✅ Retorna empresas brasileiras mockadas
- ✅ Responde a queries diferentes (tech, agro, saúde)
- ✅ Atribui source "cnpj-ws" corretamente
- ✅ Respeita limite de resultados
- ✅ Inclui informações CNPJ nas descrições

## 🔗 Testes de Integração

### **Week1MVPIntegrationTest** - Testa sistema completo:
- ✅ **Endpoints REST:** /api/companies, /api/icps funcionando
- ✅ **Lead Search:** Busca por fintech, tech, agro retorna resultados relevantes
- ✅ **CNPJ Integration:** Source específica funciona
- ✅ **Multiple Sources:** in-memory + cnpj-ws juntas
- ✅ **Score Quality:** Distribuição 45-95, categorias HOT/WARM/COLD
- ✅ **Performance:** Resposta em < 5 segundos
- ✅ **Demo Scenarios:** Queries realistas funcionam

## 📊 Relatórios Gerados

Após executar os testes:
```bash
# Ver relatório de testes
open build/reports/tests/test/index.html

# Ver relatório de cobertura  
open build/reports/jacoco/test/html/index.html
```

## ✅ Critérios de Sucesso

### **Funcional:**
- [ ] Todos os testes passam sem erros
- [ ] DataSeeder cria dados corretos
- [ ] Endpoints retornam JSON válido
- [ ] Busca de leads funciona
- [ ] CNPJ integration ativa

### **Quality:**
- [ ] Cobertura de código > 70%
- [ ] Zero warnings críticos
- [ ] Performance < 5s para searches
- [ ] Dados realistas e relevantes

### **Demo Ready:**
- [ ] Cenários de demo passam nos testes
- [ ] Empresas brasileiras presentes
- [ ] Scores fazem sentido business
- [ ] Sources attribution correta

## 🚨 Troubleshooting

### **Testes falhando:**
```bash
# Limpar e rebuild
./gradlew clean build

# Executar com mais detalhes
./gradlew test --info --stacktrace
```

### **DataSeeder não funcionando:**
- Verificar se perfil "test" está ativo
- Verificar mocks configurados corretamente
- Checar logs de criação de empresas/ICPs

### **Integration tests falhando:**
- Verificar se aplicação sobe corretamente
- Verificar se H2 database está configurado
- Checar se endpoints estão respondendo

## 🎯 Cenários de Teste Específicos

### **Busca de Fintech:**
```java
// Deve retornar: Nubank, InovaPay, etc.
LeadSearchRequest("fintech", null, null, 5)
```

### **Busca de Agronegócio:**
```java
// Deve retornar: SLC Agrícola, AgroTech, etc.  
LeadSearchRequest("agronegócio", null, null, 5)
```

### **Source Específica:**
```java
// Deve usar apenas CNPJ source
LeadSearchRequest("tecnologia SP", ["cnpj-ws"], null, 3)
```

## 📈 Métricas Esperadas

**Testes Unitários:**
- DataSeederTest: 8 testes
- CNPJLeadDiscoverySourceTest: 10 testes

**Testes Integração:**  
- Week1MVPIntegrationTest: 12 testes

**Total:** 30+ asserções validando funcionalidade completa

## 🚀 Próximos Passos

Após todos os testes passarem:
1. ✅ Deploy para ambiente de staging
2. ✅ Executar testes manuais de demo
3. ✅ Preparar apresentação para stakeholders
4. ✅ Planejar features da Semana 2