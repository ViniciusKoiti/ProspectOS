# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

ProspectOS is a **Spring Boot + Spring Modulith** B2B prospecting application with AI integration. The project follows a modular monolith architecture with strict module boundaries enforced by tests.

## Development Commands

### Build and Run
```bash
./gradlew build                # Build the project
./gradlew bootRun             # Run the application (default: mock profile)
./gradlew bootRun --args="--spring.profiles.active=development,mcp"  # Run with MCP enabled
./gradlew test                # Run all tests
./gradlew clean               # Clean build artifacts
```

### MCP Commands
```bash
# Start MCP server (STDIO + HTTP)
./gradlew bootRun --args="--spring.profiles.active=mcp"

# Test MCP tools discovery
curl http://localhost:8082/mcp/tools/list | jq .

# Test MCP tool execution
curl -X POST http://localhost:8082/mcp/tools/call \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":"1","method":"tools/call","params":{"name":"get_query_metrics","arguments":{"timeWindow":"1h"}}}'

# MCP health check
curl http://localhost:8082/actuator/health/mcp
```

### Testing
```bash
./gradlew test                               # Run all tests
./gradlew test --tests "*ModulithTest"       # Run modulith boundary tests
./gradlew test --tests "*ApplicationTests"   # Run basic Spring Boot tests
./gradlew test --tests "dev.prospectos.core.domain.CompanyTest"  # Single test class
./gradlew test --tests "*IntegrationTest"    # All integration tests
./gradlew test --tests "*McpTest"            # Run MCP-specific tests
./gradlew test --tests "*Mcp*Tools*Test"     # Run MCP tools tests
```

### Coverage Reports
Coverage report is generated at: `build/reports/jacoco/test/html/index.html`

## Architecture

### Module Structure
The application uses Spring Modulith to enforce modular boundaries:

- **Core Module** (`dev.prospectos.core`): Contains domain entities and business logic. This module should have zero dependencies on other application modules.
- **AI Module** (`dev.prospectos.ai`): AI integration services for scoring, enrichment, and strategy recommendations
- **Infrastructure Module** (`dev.prospectos.infrastructure`): JPA repositories, web controllers, and external service adapters
- **API Module** (`dev.prospectos.api`): Service interfaces and DTOs for inter-module communication

### Key Architectural Principles
- The core module must remain dependency-free from other application modules (verified by `ModulithTest.coreIsDependent()`)
- Module boundaries are strictly enforced via `ModulithTest.modulesRespectBoundaries()`
- Domain entities are located in `dev.prospectos.core.domain`

### Technology Stack
- **Java 21** with language toolchain
- **Spring Boot 3.5.10-SNAPSHOT**
- **Spring Modulith 1.4.4** for modular architecture
- **Spring AI 1.1.2** with OpenAI and Anthropic providers
- **Spring AI MCP Server 1.1.2** for Model Context Protocol integration
- **PGVector** support for semantic search capabilities
- **H2 Database** for development/testing, PostgreSQL for production
- **Gradle** for build management
- **JUnit 5** for testing

## Configuration Profiles

- **`mock`**: Default profile for local development without real AI provider dependencies
- **`development`**: Local development with extended discovery setup and real AI providers
- **`mcp`**: MCP (Model Context Protocol) server enabled for autonomous AI optimization
- **`test`**: Deterministic integration tests with in-memory database

Environment configuration is loaded via `DotenvEnvironmentPostProcessor` from `.env` file (excluded in test profile).

### Profile-specific Lead Sources
- **Production/Default**: `in-memory,vector-company` (basic semantic search)
- **Mock**: `in-memory,vector-company` (safe for local development)
- **Development**: `in-memory,scraper,llm-discovery,vector-company` (all sources enabled)
- **Test**: `in-memory,vector-company` (deterministic for tests)

## Core Module Implementation

### Domain Model (DDD)
- **Company Aggregate**: Main aggregate root with business logic for prospecting, scoring, and qualification
- **ICP Aggregate**: Ideal Customer Profile for matching criteria
- **Value Objects**:
  - `Website`: URL validation and domain extraction
  - `Email`: Email validation with corporate/personal detection
  - `Score`: Prospecting score (0-100) with categorization
- **Domain Events**:
  - `CompanyCreated`: Fired when new company is added
  - `CompanyScored`: Fired when score is updated
  - `SignalDetected`: Fired when prospecting signals are detected

### Key Business Rules
- Companies start with score 0 and status NEW
- High scores (≥80) automatically move status to REVIEWING
- Qualified prospects require valid contact info and high scores
- Contact emails must be unique within a company
- Technology signals are automatically tracked for TECHNOLOGY_ADOPTION events

### Repository Patterns
- Custom queries for prospecting analytics (score-based, industry-based)
- Support for finding qualified prospects, stale prospects, and companies with signals
- Optimized queries for dashboard and reporting features

## AI Module Features

### Supported LLM Providers
- **OpenAI GPT-4 Turbo**: Best overall quality
- **Anthropic Claude 3.5 Sonnet**: Best for complex analysis
- **Ollama**: Local, cost-free option

### AI Capabilities
- Company-to-ICP fit analysis and intelligent scoring (0-100)
- Outreach strategy recommendations and personalized message generation
- Interest signal analysis and web scraping integration

### Function Calling
LLMs can automatically call Java functions:
- `scrapeWebsite()`: Web scraping via Python integration
- `searchNews()`: News and signal search
- `analyzeSignals()`: Signal pattern analysis

## MCP (Model Context Protocol) Integration

### Overview
ProspectOS implements MCP Server capabilities using Spring AI native annotations, enabling autonomous AI systems to monitor, analyze, and optimize the prospecting pipeline in real-time.

### MCP Server Features
- **Autonomous Optimization**: AI agents can analyze metrics and automatically adjust provider routing strategies
- **Real-time Monitoring**: Continuous performance tracking with automatic alerting
- **Cost Management**: Intelligent API usage optimization based on budget constraints
- **International Lead Search**: AI-powered lead discovery with quality assessment

### Available MCP Tools

#### Query Metrics Tools
- `get_query_metrics`: Retrieve performance metrics (success rate, cost, response times)
- `analyze_query_performance`: Deep analysis of query patterns and optimization opportunities
- `get_query_trends`: Historical trend analysis for decision making

#### Provider Routing Tools
- `update_provider_routing`: Dynamic routing strategy updates (COST_OPTIMIZED, PERFORMANCE_OPTIMIZED, BALANCED)
- `get_provider_health`: Real-time status of all location search providers
- `test_provider_configuration`: Validate routing changes with sample queries

#### International Search Tools
- `search_international_leads`: Execute optimized international business searches
- `enrich_international_lead`: Enhanced lead qualification with website analysis
- `optimize_search_strategy`: AI-driven search strategy recommendations

### MCP Resources (URI Templates)
- `query-history://{timeWindow}/{provider}`: Historical query data access
- `provider-performance://{provider}/{metric}`: Provider-specific performance metrics
- `market-analysis://{country}/{industry}`: Market intelligence for search optimization

### MCP Configuration

#### Development Setup
```bash
# Start with MCP enabled
./gradlew bootRun --args="--spring.profiles.active=development,mcp"

# Verify MCP server
curl http://localhost:8082/mcp/tools/list
```

#### MCP Transport Options
- **STDIO**: For local AI clients and development (`spring.ai.mcp.server.stdio=true`)
- **HTTP**: For remote AI systems and production (`spring.ai.mcp.server.http.enabled=true`)

#### Example MCP Client Interaction
```json
// AI requests current metrics
{
  "jsonrpc": "2.0",
  "method": "tools/call",
  "params": {
    "name": "get_query_metrics",
    "arguments": {"timeWindow": "1h"}
  }
}

// AI optimizes based on high costs
{
  "jsonrpc": "2.0", 
  "method": "tools/call",
  "params": {
    "name": "update_provider_routing",
    "arguments": {
      "strategy": "COST_OPTIMIZED",
      "providerPriority": ["nominatim", "bing-maps", "google-places"]
    }
  }
}
```

### Autonomous Optimization Workflows
1. **Cost Management**: AI monitors spend and automatically switches to cheaper providers when budget thresholds are exceeded
2. **Performance Optimization**: AI detects slow responses and reroutes to faster providers
3. **Quality Assurance**: AI maintains minimum success rates by adjusting provider priorities
4. **Market Adaptation**: AI learns from search patterns and optimizes strategies per geographic region

### MCP Implementation Architecture
Located in `dev.prospectos.infrastructure.mcp`:
```
├── tools/           # @McpTool implementations
├── resources/       # @McpResource implementations  
├── config/         # MCP server configuration
└── dto/            # MCP response objects
```

## Vectorization and Semantic Search

### Backend-Switchable Architecture
- **Text Embedding Service**: Abstraction for embedding generation
- **Vector Index**: Contract for vector storage and similarity search
- **In-Memory Backend**: Fast local development with `InMemoryVectorIndex`
- **PGVector Backend**: Production-ready PostgreSQL vector storage

### Key Configuration Properties
```properties
prospectos.discovery.vector.enabled=true
prospectos.vectorization.backend=in-memory|pgvector
prospectos.vectorization.model-id=hashing-v1
prospectos.vectorization.embedding-dimension=256
prospectos.vectorization.top-k=5
prospectos.vectorization.min-similarity=0.20
```

## API Surface

### Core Endpoints
- **Companies**: `GET/POST /api/companies`, `GET/PUT/DELETE /api/companies/{id}`
- **ICPs**: `GET/POST /api/icps`, `GET/PUT/DELETE /api/icps/{id}`
- **Lead Discovery**: `POST /api/leads/discover`, `POST /api/leads/accept`
- **Enrichment**: `POST /api/prospect/enrich`

### MCP Endpoints (AI Integration)
- **Tool Discovery**: `GET /mcp/tools/list` - List available MCP tools
- **Tool Execution**: `POST /mcp/tools/call` - Execute MCP tools via JSON-RPC 2.0
- **Resource Access**: `GET /mcp/resources/{uri}` - Access MCP resources via URI templates
- **Health Check**: `GET /actuator/health/mcp` - MCP server health status

### MCP Transport Methods
- **HTTP Transport**: Port 8082 (configurable) for remote AI clients
- **STDIO Transport**: Standard input/output for local AI processes

## Testing Strategy

The project includes modulith-specific tests to ensure architectural compliance:
- Module boundary verification ensures modules don't violate encapsulation
- Core module dependency tests verify the core remains independent
- Standard Spring Boot context tests ensure proper application startup

## Technical Debt Tracking

Current technical debt is tracked in `docs/technical-debt/README.md` with detailed analysis and priority classification. Key areas of focus:
- Security: Remove committed `.env` files and migrate from SNAPSHOT dependencies
- Lead Flow: Complete preview-to-accept workflow implementation
- Product Features: ICP defaults and complete DTO mappings

## Development Workflow

### Before Making Changes
1. Run modulith tests to verify current boundaries: `./gradlew test --tests "*ModulithTest"`
2. Check current technical debt status in `docs/technical-debt/README.md`
3. Review module package-info.java files to understand architectural constraints

### After Making Changes
1. Always run full test suite: `./gradlew test`
2. Verify modulith boundaries are respected
3. Test MCP functionality if changes affect infrastructure layer: `./gradlew test --tests "*Mcp*"`
4. Update technical debt documentation if applicable
5. Ensure no new `.env` files or secrets are committed
6. Test MCP tools after infrastructure changes: `curl http://localhost:8082/mcp/tools/list`

### MCP-Specific Development Workflow

#### When Adding New MCP Tools
1. Create tool class in `dev.prospectos.infrastructure.mcp.tools`
2. Use `@McpTool` annotation with clear descriptions for AI understanding
3. Add comprehensive unit tests with mocked dependencies
4. Test via MCP client or curl commands
5. Update documentation with new tool capabilities

#### When Modifying Business Logic
1. Consider impact on MCP tools that depend on modified services
2. Update MCP tool implementations if service contracts change
3. Verify autonomous AI workflows still function correctly
4. Test end-to-end MCP scenarios after changes

#### MCP Security Considerations
- MCP tools should validate all input parameters
- Sensitive operations require proper authorization
- Rate limiting should be applied to prevent abuse
- All MCP interactions should be logged for audit purposes