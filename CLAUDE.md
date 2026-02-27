# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

ProspectOS is a **Spring Boot + Spring Modulith** B2B prospecting application with AI integration. The project follows a modular monolith architecture with strict module boundaries enforced by tests.

## Development Commands

### Build and Run
```bash
./gradlew build                # Build the project
./gradlew bootRun             # Run the application (default: mock profile)
./gradlew test                # Run all tests
./gradlew clean               # Clean build artifacts
```

### Testing
```bash
./gradlew test                               # Run all tests
./gradlew test --tests "*ModulithTest"       # Run modulith boundary tests
./gradlew test --tests "*ApplicationTests"   # Run basic Spring Boot tests
./gradlew test --tests "dev.prospectos.core.domain.CompanyTest"  # Single test class
./gradlew test --tests "*IntegrationTest"    # All integration tests
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
- **Spring AI 1.0.0-M4** with OpenAI and Anthropic providers
- **PGVector** support for semantic search capabilities
- **H2 Database** for development/testing, PostgreSQL for production
- **Gradle** for build management
- **JUnit 5** for testing

## Configuration Profiles

- **`mock`**: Default profile for local development without real AI provider dependencies
- **`development`**: Local development with extended discovery setup and real AI providers
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
3. Update technical debt documentation if applicable
4. Ensure no new `.env` files or secrets are committed