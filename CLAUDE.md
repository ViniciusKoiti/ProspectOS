# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Java Spring Boot application using Spring Modulith for modular architecture. The project is called "prospectos" and follows a modular monolith pattern to ensure proper module boundaries and dependencies.

## Development Commands

### Build and Run
```bash
./gradlew build                # Build the project
./gradlew bootRun             # Run the application
./gradlew test                # Run all tests
./gradlew clean               # Clean build artifacts
```

### Testing
```bash
./gradlew test                               # Run all tests
./gradlew test --tests "*ModulithTest"       # Run modulith boundary tests
./gradlew test --tests "*ApplicationTests"   # Run basic Spring Boot tests
```

## Architecture

### Module Structure
The application uses Spring Modulith to enforce modular boundaries:

- **Core Module** (`dev.prospectos.core`): Contains domain entities and business logic. This module should have zero dependencies on other application modules.
- **Repository Layer** (`dev.prospectos.core.repository`): Data access layer within the core module.

### Key Architectural Principles
- The core module must remain dependency-free from other application modules (verified by `ModulithTest.coreIsDependent()`)
- Module boundaries are strictly enforced via `ModulithTest.modulesRespectBoundaries()`
- Domain entities are located in `dev.prospectos.core.domain`

### Technology Stack
- **Java 21** with language toolchain
- **Spring Boot 3.5.10-SNAPSHOT**
- **Spring Modulith 1.4.4** for modular architecture
- **Gradle** for build management
- **JUnit 5** for testing

## Core Module Implementation

### Domain Model (DDD)
- **Company Aggregate**: Main aggregate root with business logic for prospecting, scoring, and qualification
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

## Testing Strategy

The project includes modulith-specific tests to ensure architectural compliance:
- Module boundary verification ensures modules don't violate encapsulation
- Core module dependency tests verify the core remains independent
- Standard Spring Boot context tests ensure proper application startup