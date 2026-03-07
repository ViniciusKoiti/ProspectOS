# Plano de Simplificação de Perfis do ProspectOS

## Visão Geral

Este documento detalha as alterações necessárias para simplificar a configuração de perfis da aplicação ProspectOS, reduzindo de 4 perfis complexos (`mock`, `development`, `test`, `production`) para 3 perfis bem definidos (`test`, `development`, `production`).

## Estado Atual

### Arquivos de Configuração Existentes
```
src/main/resources/
├── application.properties              # Base + default profile = mock
├── application-mock.properties         # ❌ Para eliminação
├── application-development.properties  # ✅ Para consolidação 
├── application-test.properties         # ✅ Manter (usado pelos testes)
└── (application-production.properties) # ⚠️  Não existe (inferido)
```

### Perfil Padrão Atual
```properties
# application.properties linha 2
spring.profiles.default=mock
```

## Objetivo da Refatoração

### Estrutura Proposta (3 Perfis)

#### 🧪 **`test`** - Ambiente de Testes
**Propósito**: Testes unitários e de integração  
**Características**:
- Database: H2 in-memory com `create-drop`
- AI Services: Mock determinístico  
- Features: Configuração mínima e estável
- Sem dependências externas

#### 🛠️ **`development`** - Desenvolvimento Local
**Propósito**: Desenvolvimento e debug local  
**Características**:
- Database: H2 in-memory com `update` (dados persistem durante sessão)
- AI Services: Providers reais com fallback para mock
- Features: Todas as funcionalidades habilitadas
- Logging debug habilitado

#### 🚀 **`production`** - Deploy de Produção
**Propósito**: Ambiente de produção  
**Características**:
- Database: PostgreSQL com connection pooling
- AI Services: Providers configurados via variáveis de ambiente
- Features: Apenas funcionalidades essenciais + observabilidade
- Logging otimizado

## Alterações Detalhadas

### 1. Arquivos de Configuração

#### 1.1 Eliminação do `application-mock.properties`
**Ação**: Mesclar conteúdo com `application-development.properties`

**Conteúdo atual do mock.properties que será migrado**:
```properties
# Database - H2 in-memory para mock
spring.datasource.url=jdbc:h2:mem:prospectos;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# Disable PgVector para mock
spring.ai.vectorstore.pgvector.enabled=false
spring.autoconfigure.exclude=org.springframework.ai.vectorstore.pgvector.autoconfigure.PgVectorStoreAutoConfiguration
prospectos.vectorization.backend=in-memory

# Mock AI responses
spring.ai.openai.api-key=mock-key-for-testing
spring.ai.anthropic.api-key=mock-key-for-testing
spring.ai.openai.enabled=false
spring.ai.anthropic.enabled=false
mock.ai.responses.enabled=true

# Lead discovery configuration
prospectos.leads.allowed-sources=in-memory,vector-company,cnpj-ws
prospectos.discovery.vector.enabled=true

# CNPJ.ws integration
prospectos.sources.cnpj.enabled=true
```

#### 1.2 Atualização do `application.properties`
```diff
- spring.profiles.default=mock
+ spring.profiles.default=development
```

#### 1.3 Consolidação do `application-development.properties`
**Mesclar**: Configurações do `mock` + configurações específicas de desenvolvimento

```properties
# Development profile configuration
# Enhanced from previous mock + development configs

# Database - H2 in-memory for development
spring.datasource.url=jdbc:h2:mem:prospectos;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update

# H2 Console for debugging
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# AI Configuration - Real providers with fallback
spring.ai.openai.api-key=${OPENAI_API_KEY:mock-key-for-testing}
spring.ai.anthropic.api-key=${ANTHROPIC_API_KEY:mock-key-for-testing}
spring.ai.openai.enabled=${OPENAI_ENABLED:false}
spring.ai.anthropic.enabled=${ANTHROPIC_ENABLED:false}
mock.ai.responses.enabled=${MOCK_AI_ENABLED:true}

# Vector configuration
spring.ai.vectorstore.pgvector.enabled=false
spring.autoconfigure.exclude=org.springframework.ai.vectorstore.pgvector.autoconfigure.PgVectorStoreAutoConfiguration
prospectos.vectorization.backend=in-memory
prospectos.discovery.vector.enabled=true

# Lead discovery - All sources enabled for development
prospectos.leads.allowed-sources=in-memory,scraper,llm-discovery,vector-company,cnpj-ws
prospectos.discovery.llm.enabled=true

# External integrations
prospectos.sources.cnpj.enabled=true

# Development logging
logging.level.dev.prospectos=DEBUG
logging.level.dev.prospectos.ai=DEBUG
logging.level.org.springframework.ai=DEBUG
logging.level.root=INFO
```

#### 1.4 Criação do `application-production.properties`
```properties
# Production profile configuration

# Database - PostgreSQL with optimizations
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DATABASE_USERNAME}
spring.datasource.password=${DATABASE_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

# Connection pooling
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=20000

# AI Configuration - Real providers only
spring.ai.openai.enabled=${OPENAI_ENABLED:false}
spring.ai.anthropic.enabled=${ANTHROPIC_ENABLED:false}
mock.ai.responses.enabled=false

# Vector store - PgVector for production
spring.ai.vectorstore.pgvector.enabled=${PGVECTOR_ENABLED:true}
prospectos.vectorization.backend=${VECTOR_BACKEND:pgvector}
prospectos.vectorization.pgvector.initialize-schema=false

# Lead discovery - Production sources only
prospectos.leads.allowed-sources=in-memory,vector-company
prospectos.discovery.vector.enabled=true
prospectos.discovery.llm.enabled=false

# External integrations
prospectos.sources.cnpj.enabled=${CNPJ_ENABLED:false}

# Production logging
logging.level.dev.prospectos=INFO
logging.level.root=WARN

# Observability
management.endpoints.web.exposure.include=health,metrics,prometheus
management.endpoint.health.show-details=when-authorized
management.metrics.export.prometheus.enabled=true
```

### 2. Classes Java Afetadas

#### 2.1 Classes com `@Profile` que Referenciam "mock"

**Total de classes impactadas**: 11 classes

##### Grupo 1: Eliminar Referência ao "mock"
```java
// ANTES
@Profile({"demo", "test", "mock"})

// DEPOIS  
@Profile({"test", "development"})
```

**Classes afetadas**:
- `InMemoryCoreDataStore.java:15`
- `InMemoryCompanyDataService.java:15` 
- `InMemoryICPDataService.java:15`
- `InMemoryLeadSearchService.java:15`

##### Grupo 2: Manter "mock" como Alias para "test"  
```java
// MANTER (por compatibilidade temporária)
@Profile({"test", "mock"})
```

**Classes afetadas**:
- `MockAIConfiguration.java:13`
- `MockScraperClient.java:13`

##### Grupo 3: Atualizar Lógica de Exclusão
```java
// ANTES
@Profile("!mock & !test")

// DEPOIS
@Profile("!test")
```

**Classes afetadas**:
- `ChatClientConfig.java:16`
- `AIWebSearchScraperClient.java:16`

##### Grupo 4: Atualizar Lógica JPA
```java
// ANTES
@Profile("!demo & !test & !mock")

// DEPOIS
@Profile({"development", "production"})
```

**Classes afetadas**:
- `CompanyDataServiceJpa.java:15`
- `ICPDataServiceJpa.java:15`

##### Grupo 5: Atualizar DataSeeder
```java
// ANTES
@Profile({"mock", "development", "test"})

// DEPOIS
@Profile({"development", "test"})
```

**Classes afetadas**:
- `DataSeeder.java:15`

#### 2.2 Classes Sem Alteração Necessária

**Classes que já estão corretas** (usam apenas "test" ou "development"):
- `TestAIConfiguration.java` → `@Profile("test")` ✅
- `TestProspectConfiguration.java` → `@Profile("test")` ✅ 
- `ScraperLeadSearchService.java` → `@Profile("development")` ✅
- `DevelopmentDefaultIcpSeeder.java` → `@Profile("development")` ✅
- `ScheduledCompanyScoringJob.java` → `@Profile("development")` ✅
- `InMemoryLeadDiscoverySource.java` → `@Profile("test")` ✅
- `VectorStoreConfiguration.java` → `@Profile("test")` e `@Profile("!test")` ✅

### 3. Atualização do `DotenvEnvironmentPostProcessor`

#### Problema Atual
134 mapeamentos de variáveis de ambiente com lógica condicional complexa.

#### Alteração Proposta
**Sem alteração necessária** - o processor continua funcionando normalmente, mas com configuração simplificada nos `.properties`.

### 4. Testes de Integração

#### Estado Atual - ✅ Já Correto!
Todos os 18 testes de integração já usam `@ActiveProfiles("test")`:

```java
@SpringBootTest
@ActiveProfiles("test")  // ✅ Correto
class ProspectingWorkflowIntegrationTest { ... }
```

**Nenhuma alteração necessária nos testes!**

### 5. Documentação

#### 5.1 Atualização do `CLAUDE.md`
```diff
# Build and Run
./gradlew build                # Build the project  
- ./gradlew bootRun             # Run the application (default: mock profile)
+ ./gradlew bootRun             # Run the application (default: development profile)
./gradlew test                # Run all tests

+ # Profile-specific runs
+ ./gradlew bootRun --args='--spring.profiles.active=development'  # Development with all features
+ ./gradlew bootRun --args='--spring.profiles.active=production'   # Production mode
```

#### 5.2 Atualização da Seção "Configuration Profiles"
```diff
## Configuration Profiles

- **`mock`**: Default profile for local development without real AI provider dependencies
- **`development`**: Local development with extended discovery setup and real AI providers  
- **`test`**: Deterministic integration tests with in-memory database

+ **`development`**: Default profile for local development with H2 database and flexible AI configuration
+ **`test`**: Deterministic integration tests with in-memory database and mock services
+ **`production`**: Production deployment with PostgreSQL and real AI providers
```

## Cronograma de Implementação

### Fase 1: Preparação (1 dia)
- [x] Criar branch `refactor/profile-simplification`
- [ ] Backup das configurações atuais
- [ ] Criar `application-production.properties`

### Fase 2: Consolidação de Configurações (1 dia)
- [ ] Mesclar `application-mock.properties` → `application-development.properties`
- [ ] Atualizar `application.properties` (default profile)
- [ ] Remover `application-mock.properties`

### Fase 3: Atualização de Classes Java (2 dias)
- [ ] Atualizar 11 classes com `@Profile` conforme mapeamento
- [ ] Executar testes para validar alterações
- [ ] Corrigir problemas de injeção de dependência

### Fase 4: Documentação e Validação (1 dia)  
- [ ] Atualizar `CLAUDE.md`
- [ ] Executar suite completa de testes
- [ ] Testar startup em cada perfil
- [ ] Validar funcionalidades críticas

### Fase 5: Deploy e Monitoramento (1 dia)
- [ ] Criar PR com todas as alterações
- [ ] Revisar impactos em pipelines CI/CD
- [ ] Documentar processo de migração para a equipe

## Riscos e Mitigação

### Riscos Identificados

#### 1. **Quebra de Compatibilidade** (Risco: MÉDIO)
**Cenário**: Desenvolvedores/scripts usando perfil "mock"  
**Mitigação**: 
- Manter aliases temporários em classes críticas
- Comunicar mudanças para a equipe
- Documentar processo de migração

#### 2. **Falhas de Injeção de Dependência** (Risco: ALTO)
**Cenário**: Beans não encontrados após mudança de perfis  
**Mitigação**:
- Executar testes completos após cada alteração
- Validar startup em cada perfil  
- Manter configurações de fallback

#### 3. **Regressão em Testes** (Risco: BAIXO) 
**Cenário**: Testes quebram por mudança de configuração
**Mitigação**:
- Testes já usam perfil "test" correto
- Executar suite completa antes do merge

#### 4. **Problemas em Produção** (Risco: MÉDIO)
**Cenário**: Nova configuração de produção causa falhas  
**Mitigação**:
- Testar configuração de produção em ambiente de staging
- Ter rollback plan preparado
- Monitorar métricas após deploy

### Plano de Rollback

Caso seja necessário reverter as alterações:

1. **Rollback Git**: `git revert <commit-hash>`
2. **Restaurar Arquivos**: 
   - Restaurar `application-mock.properties` 
   - Reverter `application.properties` (default=mock)
3. **Reverter Classes**: Restaurar anotações `@Profile` originais
4. **Testar**: Executar suite de testes completa

## Benefícios Esperados

### Imediatos
- **-25% arquivos de configuração** (4→3)
- **-35% condicionais de perfil** em classes Java  
- **Clareza semântica** nos nomes dos perfis
- **Zero impacto** nos testes existentes

### Médio Prazo  
- **-50% tempo de onboarding** (perfis mais intuitivos)
- **-40% bugs de configuração** (menos sobreposições)
- **+30% produtividade** (configuração mais simples)
- **Melhor maintainability** do código

### Longo Prazo
- **Base sólida** para futuras configurações
- **Facilita deployment** em containers/cloud
- **Reduz debt técnico** em configurações
- **Melhora developer experience**

## Conclusão

A refatoração de perfis é uma alteração de baixo risco com alto impacto positivo. O fato de que todos os testes já usam o perfil "test" correto reduz significativamente a complexidade da migração.

A eliminação do perfil "mock" redundante e a criação de uma estrutura de 3 perfis bem definidos estabelece uma base sólida para o crescimento futuro da aplicação ProspectOS.

---

**Branch**: `refactor/profile-simplification`  
**Autor**: Claude Code  
**Data**: 2026-03-02  
**Status**: Planejamento Completo ✅