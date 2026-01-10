# ProspectOS - Current System Flow Diagrams

This folder contains PlantUML diagrams documenting the current usage flows of ProspectOS.

## 📊 Available Flowcharts

### 1. [Flow Overview](current-flows-overview.puml)
- **Description**: Complete overview of all implemented flows
- **Covers**: Lead search, Company CRUD, Prospect enrichment, ICP management
- **Status**: ✅ Implemented

### 2. [Lead Search - Detailed](lead-search-detailed-flow.puml)
- **Description**: Complete lead search flow (Lead Flow A)
- **Covers**: Source validation, AI web search, automatic scoring
- **Status**: ✅ Implemented (MVP-003 complete)

### 3. [Company Management](company-management-flow.puml)
- **Description**: CRUD operations for companies
- **Covers**: Create, Read, Update, Delete, Score update
- **Status**: ✅ Implemented

### 4. [Prospect Enrichment](prospect-enrichment-flow.puml)
- **Description**: AI-powered data enrichment
- **Covers**: AI analysis, scoring, strategy recommendations
- **Status**: ✅ Implemented (basic)

### 5. [AI Scraper Integration](ai-scraper-integration-flow.puml)
- **Description**: Detailed AI scraper implementation
- **Covers**: Profile selection, AI web search, error handling
- **Status**: ✅ Implemented (MVP-003)

### 6. [Email Validation - Current State](email-validation-current-state.puml)
- **Description**: Current state vs MVP-004 requirements
- **Covers**: Email validation, normalization needs
- **Status**: 🔧 Partially implemented (MVP-004 in progress)

### 7. [ICP Management](icp-management-flow.puml)
- **Description**: Ideal Customer Profile configuration and evaluation
- **Covers**: ICP configuration, company matching, bulk evaluation
- **Status**: ✅ Implemented

## 🎯 Next Steps (MVP-004)

### Flows to Implement:

1. **Data Enrichment Service**
   - Company data normalization
   - Field cleanup
   - Structured mapping

2. **Contact Data Processing**
   - Invalid email filtering
   - Duplicate detection
   - Advanced validation

3. **Enrichment Pipeline**
   - Input/output contracts
   - Deterministic process
   - Quality scoring

## 📋 How to Use

### View Diagrams
1. Use a PlantUML viewer (VSCode extension, online viewer)
2. Or generate images: `plantuml *.puml`

### Update Diagrams
1. Edit the `.puml` files
2. Maintain consistency with current implementation
3. Document changes in MVP status

## 🔄 MVP Status

| MVP | Description | Status | Diagrams |
|-----|-------------|---------|----------|
| MVP-001 | On-demand lead search API | ✅ Complete | `lead-search-detailed-flow.puml` |
| MVP-002 | Allowed sources compliance | ✅ Complete | Integrated in flows |
| MVP-003 | Scraper integration client | ✅ Complete | `ai-scraper-integration-flow.puml` |
| MVP-004 | Enrichment and email validation | 🔧 In progress | `email-validation-current-state.puml` |
| MVP-005 | AI scoring integration | ✅ Complete | Integrated in flows |
| MVP-006 | Flow orchestration | 📝 Pending | To be created |

## 📐 Diagram Conventions

- **🔴 Red**: Not implemented
- **🔧 Yellow**: In development
- **✅ Green**: Implemented and working
- **📝 Blue**: Documentation/planning

### Colors by Layer:
- **LightBlue**: API/Controller layer
- **LightGreen**: AI/ML services
- **LightYellow**: Core domain
- **LightGray**: Infrastructure/persistence
- **LightCoral**: Requirements/TODOs