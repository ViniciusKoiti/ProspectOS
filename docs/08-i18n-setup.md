# Atividade 08: Configurar Spring i18n para Futuras Mensagens

## 🎯 Objetivo
Configurar infraestrutura de internacionalização (i18n) do Spring para preparar o sistema para futuras mensagens de usuário localizadas.

## 📋 Escopo
Implementar estrutura básica de i18n que pode ser utilizada para mensagens dinâmicas, validações e futuras interfaces de usuário.

## 🟡 Prioridade: MÉDIA
**Justificativa**: Prepara infraestrutura para crescimento internacional e facilita futuras localizações.

## 📁 Arquivos que serão Criados/Modificados
- `src/main/java/dev/prospectos/config/I18nConfig.java` (NOVO)
- `src/main/resources/messages.properties` (NOVO)
- `src/main/resources/messages_en.properties` (NOVO)
- `src/main/resources/messages_pt.properties` (NOVO)
- `src/main/java/dev/prospectos/core/MessageService.java` (NOVO)

## 📝 Tarefas

### Tarefa 8.1: Configuração do Spring i18n

**Arquivo**: `src/main/java/dev/prospectos/config/I18nConfig.java` (NOVO)

```java
package dev.prospectos.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

/**
 * Internationalization configuration for ProspectOS
 * Provides support for multiple languages in user-facing messages
 */
@Configuration
public class I18nConfig implements WebMvcConfigurer {

    /**
     * Configure MessageSource for internationalized messages
     */
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setFallbackToSystemLocale(false);
        messageSource.setDefaultLocale(Locale.ENGLISH);
        
        // Enable reloading in development
        messageSource.setCacheSeconds(3600);
        
        return messageSource;
    }

    /**
     * Configure locale resolver - defaults to English
     */
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver localeResolver = new SessionLocaleResolver();
        localeResolver.setDefaultLocale(Locale.ENGLISH);
        return localeResolver;
    }

    /**
     * Interceptor for locale changes via ?lang=en parameter
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}
```

### Tarefa 8.2: Arquivo de Mensagens Padrão (Inglês)

**Arquivo**: `src/main/resources/messages.properties` (NOVO)

```properties
# Default messages (English)
# AI Service Messages
ai.provider.openai.selected=Using OpenAI as primary provider
ai.provider.anthropic.selected=Using Anthropic as primary provider
ai.provider.ollama.selected=Using Ollama as primary provider
ai.provider.mock.selected=Using Mock provider for testing

ai.analysis.starting=AI analyzing company: {0}
ai.analysis.completed=AI analysis completed for: {0}
ai.analysis.failed=AI analysis failed for company {0}: {1}

ai.scoring.calculating=AI calculating score for: {0}
ai.scoring.completed=Score calculated: {0} ({1}) - {2}

ai.strategy.generating=AI generating strategy for: {0}
ai.strategy.completed=Strategy generated for: {0}

ai.outreach.generating=AI generating outreach for: {0}
ai.outreach.completed=Outreach message generated for: {0}

# Provider Status Messages
ai.provider.available={0} provider is available
ai.provider.unavailable={0} provider is not available
ai.provider.error=Error with {0} provider: {1}

# Decision Messages
ai.decision.investigate=Decision: INVESTIGATE - {0}
ai.decision.skip=Decision: SKIP - {0}
ai.decision.icp_fit=ICP fit: {0}

# General Messages
general.not_available=Not available
general.unknown=Unknown
general.success=Success
general.error=Error
general.processing=Processing...

# Validation Messages
validation.company.name.required=Company name is required
validation.company.website.invalid=Invalid website URL
validation.icp.theme.required=ICP theme is required
validation.score.range=Score must be between 0 and 100
```

### Tarefa 8.3: Mensagens em Inglês (Explícito)

**Arquivo**: `src/main/resources/messages_en.properties` (NOVO)

```properties
# English messages (explicit)
# AI Service Messages
ai.provider.openai.selected=Using OpenAI as primary provider
ai.provider.anthropic.selected=Using Anthropic as primary provider
ai.provider.ollama.selected=Using Ollama as primary provider
ai.provider.mock.selected=Using Mock provider for testing

ai.analysis.starting=AI analyzing company: {0}
ai.analysis.completed=AI analysis completed for: {0}
ai.analysis.failed=AI analysis failed for company {0}: {1}

ai.scoring.calculating=AI calculating score for: {0}
ai.scoring.completed=Score calculated: {0} ({1}) - {2}

ai.strategy.generating=AI generating strategy for: {0}
ai.strategy.completed=Strategy generated for: {0}

ai.outreach.generating=AI generating outreach for: {0}
ai.outreach.completed=Outreach message generated for: {0}

# Provider Status Messages
ai.provider.available={0} provider is available
ai.provider.unavailable={0} provider is not available
ai.provider.error=Error with {0} provider: {1}

# Decision Messages
ai.decision.investigate=Decision: INVESTIGATE - {0}
ai.decision.skip=Decision: SKIP - {0}
ai.decision.icp_fit=ICP fit: {0}

# General Messages
general.not_available=Not available
general.unknown=Unknown
general.success=Success
general.error=Error
general.processing=Processing...

# Validation Messages
validation.company.name.required=Company name is required
validation.company.website.invalid=Invalid website URL
validation.icp.theme.required=ICP theme is required
validation.score.range=Score must be between 0 and 100

# LLM Provider Descriptions
llm.provider.openai.description=Best overall quality
llm.provider.anthropic.description=Best complex analysis
llm.provider.ollama.description=Free, local execution
llm.provider.mock.description=For testing
```

### Tarefa 8.4: Mensagens em Português

**Arquivo**: `src/main/resources/messages_pt.properties` (NOVO)

```properties
# Portuguese messages
# AI Service Messages
ai.provider.openai.selected=Usando OpenAI como provedor principal
ai.provider.anthropic.selected=Usando Anthropic como provedor principal
ai.provider.ollama.selected=Usando Ollama como provedor principal
ai.provider.mock.selected=Usando provedor Mock para testes

ai.analysis.starting=IA analisando empresa: {0}
ai.analysis.completed=Análise de IA completada para: {0}
ai.analysis.failed=Análise de IA falhou para empresa {0}: {1}

ai.scoring.calculating=IA calculando score para: {0}
ai.scoring.completed=Score calculado: {0} ({1}) - {2}

ai.strategy.generating=IA gerando estratégia para: {0}
ai.strategy.completed=Estratégia gerada para: {0}

ai.outreach.generating=IA gerando outreach para: {0}
ai.outreach.completed=Mensagem de outreach gerada para: {0}

# Provider Status Messages
ai.provider.available=Provedor {0} está disponível
ai.provider.unavailable=Provedor {0} não está disponível
ai.provider.error=Erro no provedor {0}: {1}

# Decision Messages
ai.decision.investigate=Decisão: INVESTIGAR - {0}
ai.decision.skip=Decisão: PULAR - {0}
ai.decision.icp_fit=Adequação ao ICP: {0}

# General Messages
general.not_available=Não disponível
general.unknown=Desconhecido
general.success=Sucesso
general.error=Erro
general.processing=Processando...

# Validation Messages
validation.company.name.required=Nome da empresa é obrigatório
validation.company.website.invalid=URL do website é inválida
validation.icp.theme.required=Tema do ICP é obrigatório
validation.score.range=Score deve estar entre 0 e 100

# LLM Provider Descriptions
llm.provider.openai.description=Melhor qualidade geral
llm.provider.anthropic.description=Melhor análise complexa
llm.provider.ollama.description=Gratuito, execução local
llm.provider.mock.description=Para testes
```

### Tarefa 8.5: Service para Mensagens

**Arquivo**: `src/main/java/dev/prospectos/core/MessageService.java` (NOVO)

```java
package dev.prospectos.core;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Service for retrieving internationalized messages
 * Provides convenient methods for getting localized text
 */
@Service
public class MessageService {

    private final MessageSource messageSource;

    public MessageService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Get message using current locale
     */
    public String getMessage(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }

    /**
     * Get message with parameters using current locale
     */
    public String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }

    /**
     * Get message using specific locale
     */
    public String getMessage(String key, Locale locale) {
        return messageSource.getMessage(key, null, locale);
    }

    /**
     * Get message with parameters using specific locale
     */
    public String getMessage(String key, Locale locale, Object... args) {
        return messageSource.getMessage(key, args, locale);
    }

    /**
     * Get message with default fallback
     */
    public String getMessage(String key, String defaultMessage) {
        return messageSource.getMessage(key, null, defaultMessage, LocaleContextHolder.getLocale());
    }

    /**
     * Get message with parameters and default fallback
     */
    public String getMessage(String key, String defaultMessage, Object... args) {
        return messageSource.getMessage(key, args, defaultMessage, LocaleContextHolder.getLocale());
    }
}
```

### Tarefa 8.6: Exemplo de Uso Futuro

**Arquivo**: `src/main/java/dev/prospectos/ai/example/I18nUsageExample.java` (NOVO)

```java
package dev.prospectos.ai.example;

import dev.prospectos.core.MessageService;
import dev.prospectos.core.domain.Company;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Example demonstrating i18n message usage
 * Shows how to use MessageService for localized messages
 */
@Slf4j
@Component
public class I18nUsageExample {

    private final MessageService messageService;

    public I18nUsageExample(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * Demonstrate localized logging
     */
    public void demonstrateLocalizedMessages(Company company) {
        // Using current locale (English by default)
        String startMessage = messageService.getMessage("ai.analysis.starting", company.getName());
        log.info(startMessage);

        // Using specific locale (Portuguese)
        String ptMessage = messageService.getMessage("ai.analysis.starting", 
            Locale.forLanguageTag("pt"), company.getName());
        log.info("Portuguese: {}", ptMessage);

        // Using with fallback
        String unknownMessage = messageService.getMessage("unknown.key", 
            "Default message for unknown key");
        log.info(unknownMessage);

        // Provider status examples
        String providerAvailable = messageService.getMessage("ai.provider.available", "OpenAI");
        String providerError = messageService.getMessage("ai.provider.error", "Claude", "API key not found");
        
        log.info(providerAvailable);
        log.info(providerError);
    }

    /**
     * Example of validation messages
     */
    public void demonstrateValidationMessages() {
        // Validation message examples
        String nameRequired = messageService.getMessage("validation.company.name.required");
        String invalidWebsite = messageService.getMessage("validation.company.website.invalid");
        String scoreRange = messageService.getMessage("validation.score.range");

        log.info("Validation messages:");
        log.info(" - {}", nameRequired);
        log.info(" - {}", invalidWebsite);
        log.info(" - {}", scoreRange);
    }
}
```

## 🔧 Implementação

### Passo 1: Criar Estrutura de Diretórios
```bash
mkdir -p src/main/java/dev/prospectos/config
mkdir -p src/main/resources
```

### Passo 2: Criar Arquivos de Configuração

1. **Criar I18nConfig.java**
2. **Criar MessageService.java** 
3. **Criar arquivos .properties**
4. **Criar exemplo de uso**

### Passo 3: Testar Configuração

```bash
./gradlew compileJava
```

### Passo 4: (Opcional) Refatorar Logs Existentes

Exemplo de refatoração para usar i18n:

```java
// Antes (hard-coded)
log.info("🤖 AI generating strategy: {}", company.getName());

// Depois (usando i18n)
String message = messageService.getMessage("ai.strategy.generating", company.getName());
log.info("🤖 {}", message);
```

**Nota**: Esta refatoração é opcional e pode ser feita como atividade futura.

## 🧪 Validação

### Teste 1: Compilação
```bash
./gradlew compileJava
```

### Teste 2: Teste de MessageSource

Criar um teste simples:

```java
@SpringBootTest
class I18nConfigTest {

    @Autowired
    private MessageService messageService;

    @Test
    void testEnglishMessages() {
        String message = messageService.getMessage("ai.analysis.starting", "TestCorp");
        assertEquals("AI analyzing company: TestCorp", message);
    }

    @Test
    void testPortugueseMessages() {
        String message = messageService.getMessage("ai.analysis.starting", 
            Locale.forLanguageTag("pt"), "TestCorp");
        assertEquals("IA analisando empresa: TestCorp", message);
    }

    @Test
    void testFallbackMessage() {
        String message = messageService.getMessage("unknown.key", "Default message");
        assertEquals("Default message", message);
    }
}
```

### Teste 3: Teste do Exemplo
```bash
./gradlew test --tests "*I18nUsageExample*"
```

### Teste 4: Verificação de Arquivos Properties

```bash
# Verificar se arquivos foram criados
ls -la src/main/resources/messages*.properties

# Verificar sintaxe dos properties
# (usar IDE ou ferramentas de validação)
```

## 📋 Estrutura de Chaves de Mensagem

### Convenção de Nomenclatura:
```
<módulo>.<funcionalidade>.<ação>
```

### Exemplos:
- `ai.analysis.starting` - IA iniciando análise
- `ai.provider.available` - Provider disponível
- `validation.company.name.required` - Validação requerida
- `general.not_available` - Mensagem geral

### Parâmetros:
- `{0}`, `{1}`, etc. para substituição de valores
- Usar nomes descritivos nos comentários

## 🎯 Benefícios da Implementação

### Atual:
- ✅ Infraestrutura preparada para futuro
- ✅ Mensagens centralizadas e organizadas
- ✅ Facilita manutenção de textos

### Futuro:
- 🔮 Interface web multilíngue
- 🔮 API responses localizadas
- 🔮 Validações em múltiplos idiomas
- 🔮 Logs contextuais por locale

## 📊 Casos de Uso Futuros

### 1. API REST com Locale
```java
@RestController
public class CompanyController {
    
    @GetMapping("/api/companies/{id}/analysis")
    public ResponseEntity<?> analyzeCompany(@PathVariable Long id, 
                                          @RequestHeader(value = "Accept-Language", defaultValue = "en") String locale) {
        // Usar locale para retornar mensagens localizadas
        String message = messageService.getMessage("ai.analysis.completed", 
            Locale.forLanguageTag(locale), companyName);
        return ResponseEntity.ok(Map.of("message", message, "data", result));
    }
}
```

### 2. Validação Localizada
```java
@Component
public class CompanyValidator {
    
    public ValidationResult validate(Company company, Locale locale) {
        List<String> errors = new ArrayList<>();
        
        if (company.getName() == null) {
            errors.add(messageService.getMessage("validation.company.name.required", locale));
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
}
```

### 3. Email Templates Localizados
```java
@Service
public class EmailService {
    
    public void sendWelcomeEmail(String email, Locale locale) {
        String subject = messageService.getMessage("email.welcome.subject", locale);
        String body = messageService.getMessage("email.welcome.body", locale);
        // Enviar email...
    }
}
```

## 📋 Checklist de Conclusão

### ✅ Configuration Files
- [ ] I18nConfig.java criado e configurado
- [ ] MessageService.java implementado
- [ ] LocaleResolver configurado
- [ ] LocaleChangeInterceptor configurado

### ✅ Message Files
- [ ] messages.properties (padrão)
- [ ] messages_en.properties (inglês explícito)
- [ ] messages_pt.properties (português)
- [ ] Todas as chaves principais definidas

### ✅ Code Structure
- [ ] I18nUsageExample.java criado
- [ ] Estrutura de chaves definida
- [ ] Convenção de nomenclatura documentada

### ✅ Testing
- [ ] Compilação bem-sucedida
- [ ] MessageService funcionando
- [ ] Troca de locale funcionando
- [ ] Fallbacks funcionando

### ✅ Documentation
- [ ] Casos de uso futuros documentados
- [ ] Convenções estabelecidas
- [ ] Exemplos de implementação prontos

## 🎯 Resultado Esperado

Após completar esta atividade:
- ✅ Infraestrutura i18n completamente configurada
- ✅ Sistema preparado para múltiplos idiomas
- ✅ Mensagens centralizadas e organizadas
- ✅ Base para futuras funcionalidades internacionais
- ✅ Facilita onboarding em mercados internacionais

## 🔮 Próximos Passos (Futuro)

1. **Refatorar logs existentes** para usar MessageService
2. **Implementar validação localizada** nos DTOs
3. **Adicionar suporte a mais idiomas** (espanhol, francês, etc.)
4. **Criar interface web** com seletor de idioma
5. **Implementar emails localizados**

---

**Tempo estimado**: 45 minutos
**Pré-requisitos**: Conhecimento de Spring Boot e i18n
**Status**: FINAL - Completou todas as atividades de internacionalização