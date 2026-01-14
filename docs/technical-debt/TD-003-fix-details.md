# TD-003: Correção da Dependência de .env nos Testes

## Problema Original
Os testes de integração dependiam do arquivo `.env` através da anotação `@TestPropertySource(locations = "file:.env")`, o que criava problemas:
- Testes falhavam se o arquivo .env não existisse
- Dependência de configuração externa instável
- Dificuldade em CI/CD

## Solução Implementada

### 1. Removida Anotação @TestPropertySource
Removida a anotação `@TestPropertySource(locations = "file:.env")` de 11 arquivos de teste:
- AIProvidersIntegrationTest.java
- AIServicesIntegrationTest.java
- CompanyManagementIntegrationTest.java
- EnrichmentPipelineIntegrationTest.java
- ICPManagementIntegrationTest.java
- LeadSearchIntegrationTest.java
- ModulithBoundariesIntegrationTest.java
- ProspectEnrichmentIntegrationTest.java
- ProspectingWorkflowIntegrationTest.java
- ProspectosApplicationTests.java
- ScoringPersistenceIntegrationTest.java

### 2. Configuração via application-test.properties
Criado/atualizado `src/test/resources/application-test.properties` com:
```properties
# AI Configuration - use dummy keys for tests
spring.ai.openai.api-key=${OPENAI_API_KEY:dummy-key}
spring.ai.openai.enabled=${OPENAI_ENABLED:false}
spring.ai.anthropic.api-key=${ANTHROPIC_API_KEY:dummy-key}
spring.ai.anthropic.enabled=${ANTHROPIC_ENABLED:false}

# Lead search configuration
prospectos.leads.allowed-sources=in-memory

# Database configuration - use in-memory H2
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop

# Scraper Configuration
scraper.ai.enabled=true
scraper.ai.timeout=30s
scraper.ai.max-retries=2
```

### 3. Atualizado AIServicesIntegrationTest
- Adicionado import de `CompanyMapper`
- Atualizado método `createCompanyFromSeed()` para usar `CompanyMapper.toDomain()`
- Adicionada mensagem de erro mais informativa
- Removido import não usado de `Website`

## Dados de Teste
Os testes usam `InMemoryCoreDataStore` que carrega dados seed automaticamente:
- **Companies**: IDs 1-7 (TechCorp, CloudTech, LocalRestaurant, etc.)
- **ICPs**: ID 1 (DevOps Teams)

O `InMemoryCoreDataStore` é ativado automaticamente no perfil "test" através da anotação `@Profile({"demo", "test", "mock"})`.

## Verificação
Para verificar se os testes funcionam:

```bash
# Execute um teste específico
./gradlew test --tests "dev.prospectos.integration.AIServicesIntegrationTest"

# Execute todos os testes de integração
./gradlew test --tests "dev.prospectos.integration.*"
```

## Troubleshooting

### Teste falha com "Company with ID 1 should exist"
**Causa**: O `InMemoryCompanyDataService` não está sendo usado.

**Solução**:
1. Verifique se o teste tem `@ActiveProfiles("test")`
2. Verifique se não há conflito de perfis
3. Verifique logs de inicialização do Spring para ver qual implementação está sendo usada

### Teste falha com erro de inicialização do Spring
**Causa**: Falta de configuração no application-test.properties

**Solução**:
1. Verifique se `src/test/resources/application-test.properties` existe
2. Verifique se todas as propriedades necessárias estão configuradas
3. Adicione variáveis de ambiente se necessário:
   ```bash
   export OPENAI_API_KEY=dummy-key
   export ANTHROPIC_API_KEY=dummy-key
   ```

### Como configurar API keys reais para testes (opcional)
Se você quiser testar com APIs reais:

```bash
# Linux/Mac
export OPENAI_API_KEY=sk-your-real-key
export OPENAI_ENABLED=true

# Windows PowerShell
$env:OPENAI_API_KEY="sk-your-real-key"
$env:OPENAI_ENABLED="true"
```

## Benefícios da Correção
✅ Testes não dependem mais de arquivo .env
✅ Configuração de teste isolada e reproduzível
✅ Mais fácil executar em CI/CD
✅ Mensagens de erro mais claras
✅ Uso consistente do CompanyMapper
