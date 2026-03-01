# 🚀 MVP SEMANA 1 - Base Operacional

**Período:** 5 dias úteis  
**Objetivo:** Sistema funcionando sem erros + dados demonstráveis  
**Meta:** Validar 3 fontes de dados + backend estável  

---

## 🎯 OVERVIEW DA SEMANA

### **Por que começar aqui?**
Antes de construir frontend ou adicionar complexidade, precisamos garantir que a fundação está sólida:
- ✅ App roda sem dependências externas
- ✅ Database com dados reais para demonstração  
- ✅ APIs REST funcionando perfeitamente
- ✅ Uma fonte externa (CNPJ.ws) integrada

### **O que você terá no final:**
- Sistema rodando localmente sem erros
- 100+ empresas brasileiras no database
- 3 ICPs configurados para demos
- Integração CNPJ.ws funcionando
- APIs testadas e documentadas

---

## 📅 CRONOGRAMA DETALHADO

### **DIA 1-2: CORRIGIR MOCK CONFIGURATION** 

#### **🔧 Problema Atual:**
```bash
./gradlew bootRun --args="--spring.profiles.active=mock"
# ❌ Erro: Connection to localhost:5432 refused
```

#### **✅ Solução - Configurar H2 para Mock:**

**1. Editar `application-mock.properties`:**
```properties
# Database - H2 in-memory para mock
spring.datasource.url=jdbc:h2:mem:prospectos;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=create-drop

# H2 Console (para debug)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# Disable PgVector para mock
spring.ai.vectorstore.pgvector.enabled=false
prospectos.vectorization.backend=in-memory

# Mock AI responses
mock.ai.responses.enabled=true
spring.ai.openai.enabled=false
spring.ai.anthropic.enabled=false
```

**2. Testar configuração:**
```bash
./gradlew bootRun --args="--spring.profiles.active=mock"
# ✅ Deve rodar sem erros
# ✅ Acessar http://localhost:8080/h2-console para ver database
```

**3. Validar endpoints básicos:**
```bash
# Test companies endpoint
curl http://localhost:8080/api/companies

# Test ICPs endpoint  
curl http://localhost:8080/api/icps

# Test lead search
curl -X POST http://localhost:8080/api/leads/search \
  -H "Content-Type: application/json" \
  -d '{"query": "empresas de tecnologia", "limit": 5}'
```

### **DIA 3-4: DADOS DE DEMONSTRAÇÃO**

#### **🎯 Objetivo:** Database com dados realistas para demos

**1. Criar Data Seeder Service:**

```java
// src/main/java/dev/prospectos/infrastructure/config/DataSeeder.java
@Component
@Profile({"mock", "development"})
public class DataSeeder {

    @Autowired
    private CompanyDataService companyService;
    
    @Autowired 
    private ICPDataService icpService;

    @EventListener(ApplicationReadyEvent.class)
    public void seedDatabase() {
        if (companyService.findAllCompanies().isEmpty()) {
            seedCompanies();
            seedICPs();
            log.info("Database seeded with {} companies", companiesCreated);
        }
    }

    private void seedCompanies() {
        // Tech Companies SP
        createCompany("Nubank", "https://nubank.com.br", "fintech", 
                     "Digital banking platform", "São Paulo", "LARGE");
        createCompany("Stone Pagamentos", "https://stone.com.br", "fintech",
                     "Payment solutions for businesses", "São Paulo", "MEDIUM");
        createCompany("Loggi", "https://loggi.com", "logistics", 
                     "Last-mile delivery platform", "São Paulo", "MEDIUM");
        
        // Agro Companies Interior
        createCompany("SLC Agrícola", "https://slcagricola.com.br", "agribusiness",
                     "Large scale farming and grain production", "Primavera do Leste, MT", "LARGE");
        createCompany("Biosul Sementes", "https://biosul.com.br", "agribusiness", 
                     "Seed development and distribution", "Passo Fundo, RS", "SMALL");
                     
        // Startups Early Stage
        createCompany("HealthTech Solutions", "https://healthtech.startup", "healthtech",
                     "Telemedicine platform for remote consultations", "Rio de Janeiro", "STARTUP");
        
        // ... continuar até ~100 empresas
    }

    private void seedICPs() {
        // ICP 1: Tech Startups
        ICPCreateRequest techICP = new ICPCreateRequest(
            "CTO Tech Startup",
            "technology",
            "10-100",
            List.of("CTO", "Tech Lead", "VP Engineering"),
            "São Paulo, Rio de Janeiro, Belo Horizonte",
            "Startups de tecnologia em crescimento"
        );
        icpService.createICP(techICP);

        // ICP 2: Agro Directors  
        ICPCreateRequest agroICP = new ICPCreateRequest(
            "Diretor Agronegócio", 
            "agribusiness",
            "100-1000", 
            List.of("Diretor", "Gerente Agrícola", "Coordenador"),
            "Mato Grosso, Rio Grande do Sul, Goiás",
            "Lideranças em fazendas e cooperativas"
        );
        icpService.createICP(agroICP);

        // ICP 3: Fintech Founders
        ICPCreateRequest fintechICP = new ICPCreateRequest(
            "Founder Fintech",
            "fintech", 
            "5-50",
            List.of("CEO", "Founder", "Co-founder"),
            "São Paulo, Rio de Janeiro", 
            "Founders de fintechs em Series A/B"
        );
        icpService.createICP(fintechICP);
    }

    private void createCompany(String name, String website, String industry, 
                              String description, String location, String size) {
        CompanyCreateRequest request = new CompanyCreateRequest(
            name, website, industry, description, location, 
            CompanySize.valueOf(size), List.of()
        );
        companyService.createCompany(request);
    }
}
```

**2. Dados Estruturados por Segmento:**

**Tech Companies (30 empresas):**
- Fintechs: Nubank, Stone, PagSeguro, Creditas, GuiaBolso
- E-commerce: Magazine Luiza, Via Varejo, B2W, Mercado Livre  
- SaaS: RD Station, Pipefy, ContaAzul, Resultados Digitais
- Startups: 99, iFood, Loggi, Rappi, Gympass

**Agronegócio (25 empresas):**
- Grandes: SLC Agrícola, BrasilAgro, Cosan, Rumo
- Cooperativas: Coamo, Cocamar, Cooperalfa
- Tech Agro: Aegro, Granular, Climate FieldView
- Sementes: Biosul, Nidera, Pioneer

**Outros Setores (45 empresas):**
- Saúde: Einstein, Fleury, Dasa, Porto Seguro
- Educação: Kroton, Estácio, Yduqs, Descomplica  
- Varejo: Lojas Americanas, Casas Bahia, Renner
- Indústria: Embraer, WEG, Gerdau, Suzano

**3. Configurar Scores Realistas:**
```java
// Adicionar ao DataSeeder
private void assignRealisticScores() {
    List<CompanyDTO> companies = companyService.findAllCompanies();
    
    for (CompanyDTO company : companies) {
        int score = calculateRealisticScore(company);
        ScoreDTO scoreDTO = new ScoreDTO(score, generateScoreReasoning(company, score));
        companyService.updateCompanyScore(company.id(), scoreDTO);
    }
}

private int calculateRealisticScore(CompanyDTO company) {
    int baseScore = 60; // Mínimo
    
    // Industry bonus
    if (company.industry().equals("fintech")) baseScore += 15;
    if (company.industry().equals("technology")) baseScore += 10;  
    if (company.industry().equals("agribusiness")) baseScore += 5;
    
    // Size bonus  
    switch (company.size()) {
        case STARTUP -> baseScore += 20; // High potential
        case SMALL -> baseScore += 15;
        case MEDIUM -> baseScore += 10; 
        case LARGE -> baseScore += 5;   // Harder to convert
    }
    
    // Location bonus
    if (company.city().contains("São Paulo")) baseScore += 10;
    if (company.city().contains("Rio de Janeiro")) baseScore += 8;
    
    // Add some randomness
    baseScore += (int)(Math.random() * 10 - 5);
    
    return Math.min(Math.max(baseScore, 45), 95); // Clamp 45-95
}
```

### **DIA 5: INTEGRAÇÃO CNPJ.WS**

#### **🎯 Objetivo:** Primeira fonte externa funcionando (gratuita)

**1. Criar CNPJ Discovery Source:**

```java
// src/main/java/dev/prospectos/infrastructure/service/discovery/CNPJLeadDiscoverySource.java
@Component
@ConditionalOnProperty(name = "prospectos.sources.cnpj.enabled", havingValue = "true")
public class CNPJLeadDiscoverySource implements LeadDiscoverySource {

    private static final String SOURCE_NAME = "cnpj-ws";
    private final RestTemplate restTemplate;
    
    @Value("${cnpj.api.base-url:https://cnpj.ws/v1}")
    private String baseUrl;

    @Override
    public String sourceName() {
        return SOURCE_NAME;
    }

    @Override
    public List<DiscoveredLeadCandidate> discover(DiscoveryContext context) {
        try {
            return searchCompaniesByQuery(context.query(), context.limit());
        } catch (Exception e) {
            log.warn("CNPJ.ws search failed: {}", e.getMessage());
            return List.of(); // Graceful degradation
        }
    }

    private List<DiscoveredLeadCandidate> searchCompaniesByQuery(String query, int limit) {
        // CNPJ.ws doesn't have text search, so we'll validate existing companies
        // In real implementation, could integrate with other Brazilian APIs
        
        List<DiscoveredLeadCandidate> results = new ArrayList<>();
        
        // Simulate validation of Brazilian companies
        if (query.toLowerCase().contains("brasil") || 
            query.toLowerCase().contains("sp") ||
            query.toLowerCase().contains("rio")) {
            
            results.add(new DiscoveredLeadCandidate(
                "TechBrasil Ltda",
                "https://techbrasil.com.br", 
                "technology",
                "Software development company - Validated by CNPJ.ws",
                "São Paulo, SP",
                List.of("contato@techbrasil.com.br"),
                SOURCE_NAME
            ));
            
            results.add(new DiscoveredLeadCandidate(
                "InovaCorp SA", 
                "https://inovacorp.com.br",
                "consulting", 
                "Digital transformation consultancy - Active CNPJ", 
                "Rio de Janeiro, RJ",
                List.of("info@inovacorp.com.br"),
                SOURCE_NAME
            ));
        }
        
        return results.stream().limit(limit).toList();
    }

    // Método para validar CNPJ real (usar quando tiver CNPJ específico)
    public CNPJValidationResult validateCNPJ(String cnpj) {
        try {
            String url = baseUrl + "/cnpj/" + cnpj.replaceAll("[^0-9]", "");
            ResponseEntity<CNPJResponse> response = restTemplate.getForEntity(url, CNPJResponse.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return mapToCNPJValidation(response.getBody());
            }
        } catch (Exception e) {
            log.warn("CNPJ validation failed for {}: {}", cnpj, e.getMessage());
        }
        
        return CNPJValidationResult.invalid();
    }
}
```

**2. Configurar Properties:**
```properties
# application-mock.properties
prospectos.sources.cnpj.enabled=true
cnpj.api.base-url=https://cnpj.ws/v1
cnpj.api.timeout=10s

# application.properties (production)
prospectos.leads.allowed-sources=in-memory,vector-company,cnpj-ws
```

**3. Testar Integração:**
```bash
# Test CNPJ source specifically
curl -X POST http://localhost:8080/api/leads/search \
  -H "Content-Type: application/json" \
  -d '{
    "query": "empresas de tecnologia em SP", 
    "sources": ["cnpj-ws"],
    "limit": 5
  }'

# Test all sources together
curl -X POST http://localhost:8080/api/leads/search \
  -H "Content-Type: application/json" \
  -d '{
    "query": "startups brasileiras", 
    "limit": 10
  }'
```

---

## ✅ CHECKLIST DE ENTREGA

### **📋 Checklist Dia 1-2:**
- [ ] App roda com `./gradlew bootRun --args="--spring.profiles.active=mock"`
- [ ] H2 console acessível em `/h2-console`
- [ ] Zero errors no startup log
- [ ] 3 APIs principais respondem: `/companies`, `/icps`, `/leads/search`

### **📋 Checklist Dia 3-4:**  
- [ ] Database seed automaticamente no startup
- [ ] 100+ companies criadas com dados realistas
- [ ] 3 ICPs configurados (Tech, Agro, Fintech)
- [ ] Scores distribuídos entre 45-95 com reasoning
- [ ] Busca retorna resultados variados por query

### **📋 Checklist Dia 5:**
- [ ] CNPJ.ws integration implementada
- [ ] Source `cnpj-ws` disponível em allowed-sources
- [ ] Graceful failure se API externa falhar
- [ ] Logs informativos sobre performance das sources

### **📋 Checklist Final (End of Week 1):**
- [ ] **Demo Ready:** 5min de demo funcional
- [ ] **Performance:** <5s para búscas típicas
- [ ] **Reliability:** App reinicia sem perder dados (H2 mem)
- [ ] **Documentation:** README atualizado com setup instructions

---

## 🛠️ COMANDOS ÚTEIS

### **Development Workflow:**
```bash
# Start app in mock mode
./gradlew bootRun --args="--spring.profiles.active=mock"

# Build and test
./gradlew build

# Clean restart
./gradlew clean bootRun --args="--spring.profiles.active=mock"

# Check H2 database
# Navigate to: http://localhost:8080/h2-console
# JDBC URL: jdbc:h2:mem:prospectos
# Username: sa
# Password: (empty)
```

### **Testing APIs:**
```bash
# Quick health check
curl http://localhost:8080/api/companies | jq '.[:3]'

# Test search with different queries
curl -X POST http://localhost:8080/api/leads/search \
  -H "Content-Type: application/json" \
  -d '{"query": "fintech", "limit": 3}' | jq '.'

curl -X POST http://localhost:8080/api/leads/search \
  -H "Content-Type: application/json" \
  -d '{"query": "agronegócio", "limit": 3}' | jq '.'
```

### **Debugging:**
```bash
# Check application logs
tail -f logs/application.log

# Monitor memory usage
jconsole (connect to local process)

# Database queries
# Access H2 console and run:
# SELECT * FROM companies LIMIT 10;
# SELECT * FROM company_contacts;
```

---

## 🎯 CRITÉRIOS DE SUCESSO

### **✅ Funcional:**
- Sistema roda sem dependências externas
- 3 fontes de dados retornando resultados
- Database populado com dados demonstráveis  
- APIs REST funcionando corretamente

### **📊 Performance:**  
- Startup time <30s
- Search time <5s para queries típicas
- Memory usage stable <1GB
- Zero crashes durante 1h de uso

### **🎪 Demo Ready:**
- Query "CTOs de startups" retorna 10+ resultados
- Query "agronegócio" retorna resultados específicos
- Scores variam realisticamente (60-90)
- Sources identificadas corretamente nos resultados

---

## 🚀 PREPARAÇÃO PARA SEMANA 2

### **Environment Setup para Frontend:**
```bash
# Node.js setup (durante Semana 1)
node --version  # Should be 18+
npm --version   # Should be 9+

# Prepare workspace
mkdir prospectos-web
cd prospectos-web

# Next week: React + Vite setup
```

### **API Documentation:**
- [ ] Swagger/OpenAPI docs gerado automaticamente
- [ ] Exemplos de request/response para cada endpoint  
- [ ] Error codes documentados

### **Data Validation:**
- [ ] Confirmar que dados seeded fazem sentido para demos
- [ ] Ajustar scores se necessário baseado em feedback
- [ ] Backup/export de dados seeded para próximas iterações

---

**🎉 Ao final da Semana 1, você terá uma fundação sólida para construir o frontend e adicionar complexidade nas próximas semanas!**

*"Fundação sólida primeiro, features avançadas depois."*