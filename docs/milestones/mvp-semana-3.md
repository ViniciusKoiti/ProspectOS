# 🧠 MVP SEMANA 3 - MCP + Python Scraper

**Período:** 5 dias úteis  
**Objetivo:** Diferencial inteligente sem explodir custos  
**Meta:** IA análise de queries + scraper web funcional  
**Dependência:** ✅ Semana 1 (backend) + ✅ Semana 2 (frontend)

---

## 🎯 OVERVIEW DA SEMANA

### **Por que MCP + Scraper agora?**
Com base sólida (backend + frontend), chegou a hora de adicionar o diferencial competitivo:
- 🧠 **MCP Query Analysis:** IA entende queries em português 
- 🕷️ **Python Scraper:** Extração real de dados web
- 💰 **Custo Controlado:** Apenas R$ 20/mês vs R$ 765 MCP Chain completo
- 🚀 **Diferencial Claro:** vs Apollo/ZoomInfo que não têm análise inteligente

### **Stack Tecnológico:**
- **MCP Client:** OpenAI ou Anthropic para query analysis
- **Python Flask:** Service para web scraping  
- **BeautifulSoup:** HTML parsing e data extraction
- **Docker:** Containerização para deploy fácil
- **Rate Limiting:** Evitar bloqueios de sites

### **O que você terá no final:**
- Query analysis inteligente com IA
- Serviço Python extraindo dados reais da web
- Decisão automática de estratégia de busca
- Integração Java ↔ Python funcionando
- Sistema demo-ready com diferencial competitivo

---

## 📅 CRONOGRAMA DETALHADO

### **DIA 1-2: MCP QUERY ANALYSIS**

#### **🔧 Setup MCP Client**

**1. Configurar Dependências Maven/Gradle:**
```groovy
// build.gradle - adicionar dependências
dependencies {
    // Existing dependencies...
    
    // OpenAI/Anthropic client
    implementation 'com.theokanning.openai-gpt3-java:service:0.18.2'
    // ou para Anthropic
    implementation 'org.springframework:spring-web:6.0.2'
    
    // JSON processing
    implementation 'com.fasterxml.jackson.core:jackson-databind:2.15.2'
    
    // HTTP client
    implementation 'org.springframework.boot:spring-boot-starter-webflux'
}
```

**2. Configurar Properties:**
```properties
# application.properties
# MCP Configuration
mcp.enabled=true
mcp.provider=openai
# mcp.provider=anthropic

# OpenAI Configuration  
mcp.openai.api-key=${OPENAI_API_KEY:}
mcp.openai.model=gpt-4
mcp.openai.max-tokens=500
mcp.openai.temperature=0.1

# Anthropic Configuration (alternative)
mcp.anthropic.api-key=${ANTHROPIC_API_KEY:}  
mcp.anthropic.model=claude-3-sonnet-20240229
mcp.anthropic.max-tokens=500

# Cost Control
mcp.monthly-budget=50.00
mcp.cost-per-1k-tokens=0.01
mcp.usage-tracking-enabled=true
```

**3. Implementar MCP Client:**
```java
// src/main/java/dev/prospectos/ai/mcp/MCPClient.java
@Component
@ConditionalOnProperty(name = "mcp.enabled", havingValue = "true")
public class MCPClient {

    private final WebClient webClient;
    private final MCPProperties properties;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public MCPClient(MCPProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.webClient = WebClient.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
            .build();
    }

    public <T> T callStructured(String prompt, Class<T> responseClass) {
        try {
            String response = callRaw(prompt);
            return objectMapper.readValue(response, responseClass);
        } catch (Exception e) {
            log.warn("MCP structured call failed: {}", e.getMessage());
            throw new MCPException("Failed to parse structured response", e);
        }
    }

    public String callRaw(String prompt) {
        if ("openai".equals(properties.getProvider())) {
            return callOpenAI(prompt);
        } else if ("anthropic".equals(properties.getProvider())) {
            return callAnthropic(prompt);
        } else {
            throw new IllegalStateException("Unknown MCP provider: " + properties.getProvider());
        }
    }

    private String callOpenAI(String prompt) {
        Map<String, Object> request = Map.of(
            "model", properties.getOpenai().getModel(),
            "messages", List.of(
                Map.of("role", "user", "content", prompt)
            ),
            "max_tokens", properties.getOpenai().getMaxTokens(),
            "temperature", properties.getOpenai().getTemperature()
        );

        try {
            String response = webClient.post()
                .uri("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer " + properties.getOpenai().getApiKey())
                .header("Content-Type", "application/json")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30))
                .block();

            return extractOpenAIResponse(response);
        } catch (Exception e) {
            log.error("OpenAI API call failed", e);
            throw new MCPException("OpenAI API call failed", e);
        }
    }

    private String extractOpenAIResponse(String rawResponse) {
        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            throw new MCPException("Failed to extract OpenAI response", e);
        }
    }
}
```

#### **🧠 Query Analysis Implementation**

**4. Query Analysis Service:**
```java
// src/main/java/dev/prospectos/ai/mcp/MCPQueryAnalyzer.java
@Component
@Profile("!test")
public class MCPQueryAnalyzer {

    private final MCPClient mcpClient;
    private final QueryAnalysisCache cache;
    
    public QueryAnalysis analyze(String query) {
        // Check cache first
        QueryAnalysis cached = cache.get(query);
        if (cached != null) {
            return cached;
        }

        String prompt = buildAnalysisPrompt(query);
        
        try {
            QueryAnalysis analysis = mcpClient.callStructured(prompt, QueryAnalysis.class);
            
            // Cache successful analysis
            cache.put(query, analysis);
            
            // Track usage for cost control
            trackUsage(prompt.length(), analysis);
            
            return analysis;
        } catch (MCPException e) {
            log.warn("MCP analysis failed for query '{}', using fallback", query);
            return createFallbackAnalysis(query);
        }
    }

    private String buildAnalysisPrompt(String query) {
        return """
        Analise esta query de prospecção B2B brasileira e extraia informações estruturadas.
        
        Query: "%s"
        
        Responda APENAS com JSON válido no seguinte formato:
        {
          "intent": "find|research|validate|qualify",
          "industry": "technology|fintech|agribusiness|healthcare|education|retail|manufacturing|services|other",
          "role": "CEO|CTO|diretor|founder|gerente|coordenador|analyst|other",
          "location": "São Paulo|Rio de Janeiro|Minas Gerais|Brasil|interior|remote|not_specified",
          "company_size": "startup|pequena|media|grande|enterprise|not_specified", 
          "keywords": ["palavra1", "palavra2", "palavra3"],
          "confidence": 0.85,
          "reasoning": "Breve explicação da análise em português"
        }
        
        Regras importantes:
        - confidence entre 0.0 e 1.0 baseado na clareza da query
        - keywords: 2-5 termos mais relevantes extraídos
        - reasoning: máximo 50 palavras explicando a interpretação
        - Sempre responder em JSON válido, mesmo se query for ambígua
        """.formatted(query);
    }

    private QueryAnalysis createFallbackAnalysis(String query) {
        // Simple regex-based fallback
        return QueryAnalysis.builder()
            .intent(extractIntentFallback(query))
            .industry(extractIndustryFallback(query))
            .role(extractRoleFallback(query))
            .location(extractLocationFallback(query))
            .companySize(extractSizeFallback(query))
            .keywords(extractKeywordsFallback(query))
            .confidence(0.6) // Lower confidence for fallback
            .reasoning("Análise automática via fallback (MCP indisponível)")
            .build();
    }

    // Simple fallback methods using regex patterns
    private String extractIntentFallback(String query) {
        String lowerQuery = query.toLowerCase();
        if (lowerQuery.contains("encontrar") || lowerQuery.contains("buscar") || lowerQuery.contains("procurar")) {
            return "find";
        }
        if (lowerQuery.contains("pesquisar") || lowerQuery.contains("analisar")) {
            return "research";  
        }
        if (lowerQuery.contains("validar") || lowerQuery.contains("verificar")) {
            return "validate";
        }
        return "find"; // Default
    }
}
```

**5. Query Analysis DTOs:**
```java
// src/main/java/dev/prospectos/ai/mcp/dto/QueryAnalysis.java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryAnalysis {
    private String intent;
    private String industry;
    private String role;
    private String location;
    private String companySize;
    private List<String> keywords;
    private Double confidence;
    private String reasoning;
    
    public boolean isHighConfidence() {
        return confidence != null && confidence >= 0.8;
    }
    
    public boolean isValid() {
        return intent != null && confidence != null && confidence > 0.5;
    }
}
```

### **DIA 3: STRATEGY DECISION + INTEGRATION**

#### **🎯 Strategy Decision Logic**

**1. Strategy Decider:**
```java
// src/main/java/dev/prospectos/ai/mcp/MCPStrategyDecider.java
@Component
public class MCPStrategyDecider {

    public DiscoveryStrategy decideStrategy(QueryAnalysis analysis, List<String> availableSources) {
        if (!analysis.isValid()) {
            return createDefaultStrategy(availableSources);
        }

        StrategyBuilder strategy = StrategyBuilder.create();

        // Primary source decision based on analysis
        if ("technology".equals(analysis.getIndustry()) || "fintech".equals(analysis.getIndustry())) {
            strategy.primary("vector-company"); // Our tech database is strong
            strategy.parallel("scraper"); // Get fresh web data
        }
        
        if (analysis.getLocation() != null && analysis.getLocation().contains("Brasil")) {
            strategy.parallel("cnpj-ws"); // Always validate Brazilian companies
        }

        if (analysis.getRole() != null && !analysis.getRole().equals("not_specified")) {
            strategy.parallel("scraper"); // Scraping good for finding specific roles
        }

        // Confidence-based adjustments
        if (analysis.isHighConfidence()) {
            strategy.executionMode("parallel"); // High confidence = aggressive search
            strategy.confidenceThreshold(0.7);
        } else {
            strategy.executionMode("sequential"); // Low confidence = careful search
            strategy.confidenceThreshold(0.6);
        }

        // Fallback chain
        strategy.fallback(Arrays.asList("in-memory", "vector-company"));

        return strategy.build();
    }

    private DiscoveryStrategy createDefaultStrategy(List<String> availableSources) {
        return DiscoveryStrategy.builder()
            .primarySource("in-memory")
            .parallelSources(Arrays.asList("vector-company"))
            .executionMode("sequential")
            .confidenceThreshold(0.5)
            .fallbackChain(availableSources)
            .reasoning("Estratégia padrão devido à análise inconclusiva")
            .build();
    }
}
```

**2. Integration com Discovery Service:**
```java
// Modificar DefaultLeadDiscoveryService para usar MCP
@Service
public class EnhancedLeadDiscoveryService implements LeadDiscoveryService {

    private final MCPQueryAnalyzer queryAnalyzer;
    private final MCPStrategyDecider strategyDecider;
    private final Map<String, LeadDiscoverySource> sourceRegistry;

    @Override
    public LeadSearchResponse discoverLeads(LeadDiscoveryRequest request) {
        Instant startTime = Instant.now();

        // 1. MCP Query Analysis
        QueryAnalysis analysis = queryAnalyzer.analyze(request.query());
        log.info("Query analysis completed: intent={}, confidence={}", 
                analysis.getIntent(), analysis.getConfidence());

        // 2. Strategy Decision  
        List<String> availableSources = getAvailableSources(request.sources());
        DiscoveryStrategy strategy = strategyDecider.decideStrategy(analysis, availableSources);
        log.info("Strategy decided: primary={}, parallel={}", 
                strategy.getPrimarySource(), strategy.getParallelSources());

        // 3. Execute Strategy
        DiscoveryContext context = DiscoveryContext.builder()
            .query(request.query().trim())
            .role(request.role())
            .limit(request.limit() == null ? DEFAULT_LIMIT : request.limit())
            .icp(resolveICP(request.icpId()))
            .analysis(analysis)
            .strategy(strategy)
            .build();

        List<DiscoveredLeadCandidate> discovered = executeStrategy(context, strategy);

        // 4. Return Enhanced Response
        Duration executionTime = Duration.between(startTime, Instant.now());
        
        return LeadSearchResponse.builder()
            .status(LeadSearchStatus.COMPLETED)
            .leads(toLeadResults(discovered, context.icp(), context.limit()))
            .queryId(UUID.randomUUID())
            .message(buildResponseMessage(discovered.size(), analysis, executionTime))
            .analysis(analysis) // Include analysis in response
            .strategy(strategy) // Include strategy for debugging
            .build();
    }

    private List<DiscoveredLeadCandidate> executeStrategy(DiscoveryContext context, DiscoveryStrategy strategy) {
        if ("parallel".equals(strategy.getExecutionMode())) {
            return executeParallel(context, strategy);
        } else {
            return executeSequential(context, strategy);
        }
    }

    private String buildResponseMessage(int resultCount, QueryAnalysis analysis, Duration executionTime) {
        return String.format(
            "Encontrados %d prospects em %.1fs. Análise: %s (confiança: %.0f%%)",
            resultCount,
            executionTime.toMillis() / 1000.0,
            analysis.getReasoning(),
            analysis.getConfidence() * 100
        );
    }
}
```

### **DIA 4-5: PYTHON SCRAPER SERVICE**

#### **🕷️ Flask Scraper Service**

**1. Setup Python Environment:**
```bash
# Create scraper service directory
mkdir scraper-service
cd scraper-service

# Create virtual environment
python -m venv venv
source venv/bin/activate  # Linux/Mac
# or
venv\Scripts\activate     # Windows

# Install dependencies
pip install flask requests beautifulsoup4 lxml
pip install python-dotenv redis flask-limiter
pip install docker gunicorn

# Create requirements.txt
pip freeze > requirements.txt
```

**2. Flask Application:**
```python
# scraper-service/app.py
from flask import Flask, request, jsonify
from flask_limiter import Limiter
from flask_limiter.util import get_remote_address
import requests
from bs4 import BeautifulSoup
import re
import time
import logging
from urllib.parse import urljoin, urlparse
import os
from typing import List, Dict, Optional

# Setup logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Initialize Flask
app = Flask(__name__)

# Rate limiting to prevent abuse
limiter = Limiter(
    app,
    key_func=get_remote_address,
    default_limits=["200 per day", "50 per hour", "1 per second"]
)

# Configuration
USER_AGENTS = [
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36',
    'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36',
    'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36'
]

REQUEST_TIMEOUT = 10
MAX_RETRIES = 2

class ScraperService:
    
    @staticmethod
    def scrape_website(url: str, extract_options: Dict = None) -> Dict:
        """
        Scrape a website and extract structured data
        """
        try:
            # Validate URL
            parsed_url = urlparse(url)
            if not parsed_url.scheme or not parsed_url.netloc:
                raise ValueError(f"Invalid URL: {url}")

            # Get page content
            response = ScraperService._get_page_with_retry(url)
            if not response:
                return {"error": "Failed to fetch page", "url": url}

            # Parse HTML
            soup = BeautifulSoup(response.text, 'lxml')
            
            # Extract data based on options
            extract_options = extract_options or ["emails", "phones", "description"]
            
            result = {
                "url": url,
                "title": ScraperService._extract_title(soup),
                "meta_description": ScraperService._extract_meta_description(soup),
                "success": True
            }

            if "emails" in extract_options:
                result["emails"] = ScraperService._extract_emails(soup, response.text)
            
            if "phones" in extract_options:
                result["phones"] = ScraperService._extract_phones(soup, response.text)
                
            if "description" in extract_options:
                result["description"] = ScraperService._extract_description(soup)
                
            if "tech_stack" in extract_options:
                result["tech_stack"] = ScraperService._detect_technologies(soup, response.text)
                
            if "contacts" in extract_options:
                result["contacts"] = ScraperService._extract_contacts(soup)

            return result

        except Exception as e:
            logger.error(f"Scraping failed for {url}: {str(e)}")
            return {
                "error": str(e),
                "url": url,
                "success": False
            }

    @staticmethod
    def _get_page_with_retry(url: str) -> Optional[requests.Response]:
        """
        Get page content with retries and error handling
        """
        headers = {
            'User-Agent': USER_AGENTS[hash(url) % len(USER_AGENTS)],
            'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
            'Accept-Language': 'pt-BR,pt;q=0.9,en;q=0.8',
            'Accept-Encoding': 'gzip, deflate',
            'Connection': 'keep-alive',
        }

        for attempt in range(MAX_RETRIES + 1):
            try:
                time.sleep(attempt * 1.0)  # Progressive delay
                
                response = requests.get(
                    url, 
                    headers=headers, 
                    timeout=REQUEST_TIMEOUT,
                    allow_redirects=True,
                    verify=False  # For demo purposes only
                )
                
                if response.status_code == 200:
                    return response
                    
                logger.warning(f"HTTP {response.status_code} for {url}, attempt {attempt + 1}")
                
            except requests.RequestException as e:
                logger.warning(f"Request failed for {url}, attempt {attempt + 1}: {str(e)}")
                
        return None

    @staticmethod
    def _extract_emails(soup: BeautifulSoup, raw_text: str) -> List[str]:
        """Extract email addresses from page"""
        email_pattern = r'\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}\b'
        emails = set()
        
        # Extract from raw text
        raw_emails = re.findall(email_pattern, raw_text, re.IGNORECASE)
        emails.update(raw_emails)
        
        # Extract from mailto links
        mailto_links = soup.find_all('a', href=re.compile(r'^mailto:', re.IGNORECASE))
        for link in mailto_links:
            email = link.get('href').replace('mailto:', '').split('?')[0]
            emails.add(email)
        
        # Filter out common generic emails
        filtered_emails = [
            email for email in emails 
            if not any(generic in email.lower() for generic in [
                'noreply', 'no-reply', 'admin', 'webmaster', 'info@example', 'test@'
            ])
        ]
        
        return list(filtered_emails)[:5]  # Max 5 emails

    @staticmethod
    def _extract_phones(soup: BeautifulSoup, raw_text: str) -> List[str]:
        """Extract phone numbers (Brazilian format)"""
        # Brazilian phone patterns
        phone_patterns = [
            r'\(?\d{2}\)?\s*\d{4,5}[-\s]?\d{4}',  # (11) 99999-9999 or 11 99999-9999
            r'\+55\s*\(?\d{2}\)?\s*\d{4,5}[-\s]?\d{4}',  # +55 11 99999-9999
            r'\d{2}\s*\d{4,5}[-\s]?\d{4}',  # 11999999999
        ]
        
        phones = set()
        for pattern in phone_patterns:
            matches = re.findall(pattern, raw_text)
            phones.update(matches)
            
        # Clean and format
        cleaned_phones = []
        for phone in phones:
            # Remove non-digits
            digits_only = re.sub(r'\D', '', phone)
            # Brazilian mobile: 11 digits, landline: 10 digits (after area code)
            if len(digits_only) >= 10:
                cleaned_phones.append(phone.strip())
                
        return list(set(cleaned_phones))[:3]  # Max 3 phones

    @staticmethod
    def _extract_title(soup: BeautifulSoup) -> str:
        """Extract page title"""
        title_tag = soup.find('title')
        return title_tag.get_text().strip() if title_tag else ""

    @staticmethod
    def _extract_meta_description(soup: BeautifulSoup) -> str:
        """Extract meta description"""
        meta_desc = soup.find('meta', attrs={'name': 'description'})
        return meta_desc.get('content', '').strip() if meta_desc else ""

    @staticmethod
    def _extract_description(soup: BeautifulSoup) -> str:
        """Extract company description from various sources"""
        # Try meta description first
        description = ScraperService._extract_meta_description(soup)
        if description:
            return description
            
        # Try common about sections
        about_selectors = [
            '.about', '.sobre', '.company-description',
            '#about', '#sobre', '.intro', '.description'
        ]
        
        for selector in about_selectors:
            element = soup.select_one(selector)
            if element:
                text = element.get_text().strip()
                if len(text) > 50:  # Minimum meaningful length
                    return text[:300]  # Max 300 chars
                    
        # Fallback: first paragraph with substantial text
        paragraphs = soup.find_all('p')
        for p in paragraphs:
            text = p.get_text().strip()
            if len(text) > 100:
                return text[:300]
                
        return ""

    @staticmethod
    def _detect_technologies(soup: BeautifulSoup, raw_text: str) -> List[str]:
        """Detect technologies used by the website"""
        technologies = set()
        
        # Check script sources
        scripts = soup.find_all('script', src=True)
        for script in scripts:
            src = script.get('src', '').lower()
            if 'react' in src:
                technologies.add('React')
            if 'angular' in src:
                technologies.add('Angular')
            if 'vue' in src:
                technologies.add('Vue.js')
            if 'jquery' in src:
                technologies.add('jQuery')
                
        # Check meta tags
        generator = soup.find('meta', attrs={'name': 'generator'})
        if generator:
            content = generator.get('content', '').lower()
            if 'wordpress' in content:
                technologies.add('WordPress')
            if 'shopify' in content:
                technologies.add('Shopify')
                
        # Check common CDNs in raw text
        if 'cloudflare' in raw_text.lower():
            technologies.add('Cloudflare')
        if 'amazon' in raw_text.lower() and 'aws' in raw_text.lower():
            technologies.add('AWS')
            
        return list(technologies)

# Flask Routes
@app.route('/health', methods=['GET'])
def health_check():
    return jsonify({"status": "healthy", "service": "scraper"})

@app.route('/scrape/website', methods=['POST'])
@limiter.limit("10 per minute")
def scrape_website():
    """
    Scrape a website and return structured data
    
    POST /scrape/website
    {
        "url": "https://example.com",
        "extract": ["emails", "phones", "description", "tech_stack"]
    }
    """
    try:
        data = request.get_json()
        
        if not data or 'url' not in data:
            return jsonify({"error": "URL is required"}), 400
            
        url = data['url']
        extract_options = data.get('extract', ["emails", "phones", "description"])
        
        result = ScraperService.scrape_website(url, extract_options)
        
        return jsonify(result)
        
    except Exception as e:
        logger.error(f"API error: {str(e)}")
        return jsonify({"error": "Internal server error"}), 500

@app.route('/scrape/batch', methods=['POST'])
@limiter.limit("5 per minute")
def scrape_batch():
    """
    Scrape multiple websites
    
    POST /scrape/batch  
    {
        "urls": ["https://site1.com", "https://site2.com"],
        "extract": ["emails", "description"]
    }
    """
    try:
        data = request.get_json()
        
        if not data or 'urls' not in data:
            return jsonify({"error": "URLs array is required"}), 400
            
        urls = data['urls'][:5]  # Max 5 URLs per batch
        extract_options = data.get('extract', ["emails", "phones", "description"])
        
        results = []
        for url in urls:
            result = ScraperService.scrape_website(url, extract_options)
            results.append(result)
            time.sleep(1)  # Respectful delay between requests
            
        return jsonify({
            "results": results,
            "total": len(results)
        })
        
    except Exception as e:
        logger.error(f"Batch API error: {str(e)}")
        return jsonify({"error": "Internal server error"}), 500

if __name__ == '__main__':
    port = int(os.environ.get('PORT', 3000))
    app.run(host='0.0.0.0', port=port, debug=False)
```

**3. Docker Setup:**
```dockerfile
# scraper-service/Dockerfile
FROM python:3.11-slim

WORKDIR /app

# Install system dependencies
RUN apt-get update && apt-get install -y \
    gcc \
    && rm -rf /var/lib/apt/lists/*

# Copy requirements first for better caching
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy application
COPY . .

# Create non-root user
RUN useradd --create-home --shell /bin/bash scraper
USER scraper

# Expose port
EXPOSE 3000

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:3000/health || exit 1

# Run application
CMD ["gunicorn", "--bind", "0.0.0.0:3000", "--workers", "2", "--timeout", "30", "app:app"]
```

**4. Java Integration:**
```java
// src/main/java/dev/prospectos/infrastructure/service/discovery/ScraperLeadDiscoverySource.java
@Component
@ConditionalOnProperty(name = "prospectos.sources.scraper.enabled", havingValue = "true")
public class ScraperLeadDiscoverySource implements LeadDiscoverySource {

    private static final String SOURCE_NAME = "scraper";
    private final WebClient webClient;
    private final ScraperProperties properties;
    
    @Value("${scraper.service.url:http://localhost:3000}")
    private String scraperServiceUrl;

    @Override
    public List<DiscoveredLeadCandidate> discover(DiscoveryContext context) {
        try {
            // Extract potential company websites from query
            List<String> websites = extractWebsitesFromQuery(context.getQuery());
            
            if (websites.isEmpty()) {
                // Fallback: generate potential websites based on query
                websites = generatePotentialWebsites(context.getQuery());
            }
            
            return scrapeWebsites(websites, context.getLimit());
            
        } catch (Exception e) {
            log.warn("Scraper discovery failed: {}", e.getMessage());
            return List.of(); // Graceful degradation
        }
    }

    private List<DiscoveredLeadCandidate> scrapeWebsites(List<String> websites, int limit) {
        List<DiscoveredLeadCandidate> candidates = new ArrayList<>();
        
        for (String website : websites.subList(0, Math.min(websites.size(), limit))) {
            try {
                ScrapingResult result = scrapeWebsite(website);
                if (result.isSuccess()) {
                    DiscoveredLeadCandidate candidate = mapToCandidate(result, website);
                    candidates.add(candidate);
                }
            } catch (Exception e) {
                log.debug("Failed to scrape {}: {}", website, e.getMessage());
            }
            
            // Rate limiting
            try {
                Thread.sleep(1000); // 1 second between requests
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        return candidates;
    }

    private ScrapingResult scrapeWebsite(String url) {
        Map<String, Object> request = Map.of(
            "url", url,
            "extract", List.of("emails", "phones", "description", "tech_stack")
        );

        try {
            WebClient.ResponseSpec response = webClient.post()
                .uri(scraperServiceUrl + "/scrape/website")
                .bodyValue(request)
                .retrieve();
                
            return response.bodyToMono(ScrapingResult.class)
                .timeout(Duration.ofSeconds(15))
                .block();
                
        } catch (Exception e) {
            log.warn("Scraping request failed for {}: {}", url, e.getMessage());
            throw new ScrapingException("Failed to scrape website", e);
        }
    }

    private DiscoveredLeadCandidate mapToCandidate(ScrapingResult result, String website) {
        return new DiscoveredLeadCandidate(
            result.getTitle() != null ? result.getTitle() : extractCompanyNameFromUrl(website),
            website,
            inferIndustryFromDescription(result.getDescription()),
            result.getDescription() != null ? result.getDescription() : "Empresa descoberta via web scraping",
            "Brasil", // Default location
            result.getEmails() != null ? result.getEmails() : List.of(),
            SOURCE_NAME
        );
    }

    private List<String> generatePotentialWebsites(String query) {
        // Simple heuristic to generate potential company websites
        List<String> keywords = extractKeywords(query);
        List<String> websites = new ArrayList<>();
        
        for (String keyword : keywords) {
            // Common Brazilian domain patterns
            websites.add("https://" + keyword.toLowerCase() + ".com.br");
            websites.add("https://" + keyword.toLowerCase() + ".com");
            
            // Remove spaces and special chars
            String clean = keyword.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
            if (!clean.equals(keyword.toLowerCase())) {
                websites.add("https://" + clean + ".com.br");
            }
        }
        
        return websites.subList(0, Math.min(websites.size(), 5)); // Max 5 attempts
    }
}
```

---

## ✅ CHECKLIST DE ENTREGA

### **📋 Checklist Dia 1-2:**
- [ ] MCP Client configurado (OpenAI ou Anthropic)
- [ ] Properties com API keys configuradas
- [ ] Query Analysis funcionando com prompts em português
- [ ] Fallback system para quando MCP falhar
- [ ] Cost tracking implementado

### **📋 Checklist Dia 3:**
- [ ] Strategy Decider implementado
- [ ] Integração com Discovery Service
- [ ] Lógica de decisão baseada em análise
- [ ] Execution modes (parallel/sequential)
- [ ] Enhanced response com analysis + strategy

### **📋 Checklist Dia 4:**
- [ ] Python Flask service funcionando
- [ ] Scraping basic (emails, phones, description)
- [ ] Rate limiting implementado
- [ ] Docker container configurado
- [ ] Health check endpoint

### **📋 Checklist Dia 5:**
- [ ] Java ↔ Python integration funcionando
- [ ] Scraper Discovery Source implementado
- [ ] Error handling e graceful degradation
- [ ] Performance otimizada (caching, timeouts)
- [ ] Full end-to-end test funcionando

### **📋 Checklist Final (End of Week 3):**
- [ ] **Query Intelligence:** IA analisa queries em português
- [ ] **Smart Strategy:** Sistema decide automaticamente quais fontes usar
- [ ] **Web Scraping:** Extrai dados reais de websites brasileiros
- [ ] **Cost Control:** Budget tracking para MCP usage
- [ ] **Demo Ready:** Diferencial claro vs competidores

---

## 🛠️ COMANDOS ÚTEIS

### **MCP Testing:**
```bash
# Test MCP analysis
curl -X POST http://localhost:8080/api/leads/discover \
  -H "Content-Type: application/json" \
  -d '{
    "query": "CTOs de startups fintech em São Paulo",
    "limit": 5
  }'

# Check analysis in response  
jq '.analysis' response.json
```

### **Python Scraper:**
```bash
# Start scraper service
cd scraper-service
python app.py

# Test scraper directly
curl -X POST http://localhost:3000/scrape/website \
  -H "Content-Type: application/json" \
  -d '{"url": "https://nubank.com.br", "extract": ["emails", "description"]}'

# Test batch scraping
curl -X POST http://localhost:3000/scrape/batch \
  -H "Content-Type: application/json" \
  -d '{"urls": ["https://stone.com.br", "https://pipefy.com"], "extract": ["emails"]}'
```

### **Integration Testing:**
```bash
# Full integration test
# 1. Start Python scraper
cd scraper-service && python app.py &

# 2. Start Java backend
./gradlew bootRun --args="--spring.profiles.active=mock" &

# 3. Test full flow
curl -X POST http://localhost:8080/api/leads/search \
  -H "Content-Type: application/json" \
  -d '{
    "query": "empresas de tecnologia que usam React",
    "sources": ["scraper", "cnpj-ws"],
    "limit": 3
  }'
```

### **Docker Deployment:**
```bash
# Build scraper image
cd scraper-service
docker build -t prospectos/scraper:latest .

# Run with docker-compose
# Add to docker-compose.yml:
version: '3.8'
services:
  scraper:
    image: prospectos/scraper:latest
    ports:
      - "3000:3000"
    environment:
      - PORT=3000
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:3000/health"]
      interval: 30s
      timeout: 10s
      retries: 3
```

---

## 🎯 CRITÉRIOS DE SUCESSO

### **✅ Funcional:**
- MCP análise funcionando com >80% accuracy
- Strategy decision automática baseada em análise
- Python scraper extraindo dados reais
- Java ↔ Python communication estável
- Error handling robusto em todas as camadas

### **🧠 Intelligence:**
- Query "CTOs fintech SP" → strategy: scraper + cnpj-ws
- Query "fazendas MT" → strategy: cnpj-ws + vector
- Analysis confidence >0.8 para queries claras
- Fallback graceful quando MCP indisponível

### **💰 Cost Control:**
- MCP usage <R$ 20/mês para 500 análises
- Rate limiting previne abuse
- Caching reduz calls desnecessárias
- Monitoring de budget funcionando

### **🚀 Performance:**
- MCP analysis <3s response time
- Scraper <10s por website
- Parallel execution when appropriate
- Graceful timeouts e retries

---

## 🎪 DEMO ENHANCEMENT

### **Before (Semana 2):**
```
Input: "CTOs fintech São Paulo"
Processing: Loop básico por fontes fixas
Output: Resultados genéricos sem contexto
```

### **After (Semana 3):**
```
Input: "CTOs fintech São Paulo"

🧠 Análise IA: 
   Intent: find
   Industry: fintech
   Role: CTO  
   Location: São Paulo
   Confidence: 94%

🎯 Estratégia:
   Primary: scraper (roles específicos)
   Parallel: cnpj-ws (validação BR)
   Mode: parallel (high confidence)

📊 Resultados:
   12 prospects encontrados em 8s
   3 via scraper (sites fintech SP)  
   5 via cnpj-ws (empresas validadas)
   4 via vector (similares na base)

💡 Diferencial claro: "Análise inteligente + dados frescos da web"
```

---

**🎉 Ao final da Semana 3, você terá o diferencial competitivo funcionando: IA que entende português + scraper que busca dados frescos + decisão automática de estratégia!**

*"Inteligência artificial + dados frescos = competitive moat"*