# 🔍 Guia de Ferramentas de Qualidade - ProspectOS

Este documento explica como usar as ferramentas de qualidade de código configuradas no projeto.

## 📊 Ferramentas Configuradas

### 🎯 **JaCoCo** - Cobertura de Código
Mede a cobertura de testes do código.

**Comandos:**
```bash
# Gerar relatório de cobertura
./gradlew jacocoTestReport

# Verificar thresholds de cobertura
./gradlew jacocoTestCoverageVerification
```

**Relatórios:**
- HTML: `build/reports/jacoco/test/html/index.html`
- XML: `build/reports/jacoco/test/jacocoTestReport.xml`
- CSV: `build/reports/jacoco/test/jacocoTestReport.csv`

**Thresholds Configurados:**
- Cobertura de Instruções: **≥60%**
- Cobertura de Branches: **≥50%**
- Classes de Negócio: **≥70%**

### 🐛 **SpotBugs** - Detecção de Bugs
Encontra bugs potenciais e problemas de código.

**Comandos:**
```bash
# Analisar código principal
./gradlew spotbugsMain

# Analisar código de teste
./gradlew spotbugsTest
```

**Relatórios:**
- HTML: `build/reports/spotbugs/main.html`
- XML: `build/reports/spotbugs/main.xml`

**Configuração:**
- Effort: **Max** (análise mais profunda)
- Report Level: **Medium** (balance entre precisão e velocidade)
- Exclusões: `config/spotbugs/spotbugs-exclude.xml`

### 📏 **PMD** - Análise de Código
Detecta problemas de estilo, design e performance.

**Comandos:**
```bash
# Analisar código principal
./gradlew pmdMain

# Analisar código de teste
./gradlew pmdTest
```

**Relatórios:**
- HTML: `build/reports/pmd/main.html`
- XML: `build/reports/pmd/main.xml`

**Regras Configuradas:**
- Best Practices ✅
- Code Style ✅ (ajustado para Spring Boot)
- Design ✅ (complexidade ciclomática ≤15)
- Error Prone ✅
- Performance ✅
- Security ✅

### 🔬 **SonarQube** - Análise Completa
Análise abrangente de qualidade, segurança e maintainability.

**Pré-requisitos:**
```bash
# Instalar SonarQube localmente (Docker)
docker run -d --name sonarqube -p 9000:9000 sonarqube:community

# Ou usar SonarCloud (https://sonarcloud.io)
```

**Comandos:**
```bash
# Análise local (após configurar SonarQube)
./gradlew sonarqube -Dsonar.host.url=http://localhost:9000 -Dsonar.login=TOKEN

# Análise no SonarCloud
./gradlew sonarqube -Dsonar.organization=your-org -Dsonar.login=TOKEN
```

**Métricas Analisadas:**
- Code Coverage (via JaCoCo)
- Bugs (via SpotBugs)
- Code Smells (via PMD)
- Security Hotspots
- Duplicação de Código
- Complexidade Ciclomática
- Maintainability Rating

## 🚀 Comando Integrado

### **Quality Check** - Execução Completa
```bash
# Roda TODOS os checks de qualidade
./gradlew qualityCheck
```

**O que inclui:**
1. Testes unitários e de integração
2. Relatório de cobertura JaCoCo
3. Verificação de thresholds de cobertura
4. Análise SpotBugs
5. Análise PMD

**Relatórios gerados:**
```
build/reports/
├── jacoco/test/html/index.html     # Cobertura
├── spotbugs/main.html              # Bugs detectados
└── pmd/main.html                   # Problemas de código
```

## 📈 Integração com CI/CD

### **GitHub Actions**
```yaml
# .github/workflows/quality-check.yml
name: Quality Check

on: [push, pull_request]

jobs:
  quality:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'
          
      - name: Run Quality Checks
        run: ./gradlew qualityCheck
        
      - name: SonarQube Analysis
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
        run: ./gradlew sonarqube
        
      - name: Upload Coverage to Codecov
        uses: codecov/codecov-action@v3
        with:
          file: build/reports/jacoco/test/jacocoTestReport.xml
```

### **GitLab CI**
```yaml
# .gitlab-ci.yml
quality_check:
  stage: test
  script:
    - ./gradlew qualityCheck
    - ./gradlew sonarqube -Dsonar.qualitygate.wait=true
  artifacts:
    reports:
      junit: build/test-results/test/TEST-*.xml
      coverage_report:
        coverage_format: jacoco
        path: build/reports/jacoco/test/jacocoTestReport.xml
```

## 🎯 Metas de Qualidade

### **Cobertura de Código**
- **Atual**: 66%
- **Meta Curto Prazo**: 75%
- **Meta Longo Prazo**: 85%

### **Qualidade SonarQube**
- **Maintainability Rating**: A
- **Reliability Rating**: A  
- **Security Rating**: A
- **Duplicação**: <3%

### **SpotBugs**
- **Bugs de Alta Prioridade**: 0
- **Bugs de Média Prioridade**: <5

### **PMD**
- **Violações de Alta Prioridade**: 0
- **Complexidade Ciclomática**: <15 por método

## 🛠️ Configuração Personalizada

### **Ajustar Thresholds de Cobertura**
```gradle
// build.gradle
jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = 0.80 // Aumentar para 80%
            }
        }
    }
}
```

### **Personalizar Regras PMD**
Edite: `config/pmd/pmd-rules.xml`

### **Personalizar Exclusões SpotBugs**
Edite: `config/spotbugs/spotbugs-exclude.xml`

### **Configurar SonarQube**
```gradle
// build.gradle
sonarqube {
    properties {
        property 'sonar.projectKey', 'seu-projeto'
        property 'sonar.organization', 'sua-org'
        // ... outras propriedades
    }
}
```

## 📚 Recursos Adicionais

### **Documentação**
- [JaCoCo Documentation](https://www.jacoco.org/jacoco/trunk/doc/)
- [SpotBugs Manual](https://spotbugs.github.io/spotbugs-manual/)
- [PMD Rules](https://pmd.github.io/pmd/pmd_rules_java.html)
- [SonarQube Documentation](https://docs.sonarqube.org/)

### **IDEs Integration**
- **IntelliJ IDEA**: Plugins disponíveis para SonarLint, SpotBugs
- **VS Code**: Extensions para SonarLint, PMD
- **Eclipse**: Plugins nativos para SpotBugs, PMD

### **Badges para README**
```markdown
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=YOUR_PROJECT&metric=coverage)](https://sonarcloud.io/dashboard?id=YOUR_PROJECT)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=YOUR_PROJECT&metric=alert_status)](https://sonarcloud.io/dashboard?id=YOUR_PROJECT)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=YOUR_PROJECT&metric=bugs)](https://sonarcloud.io/dashboard?id=YOUR_PROJECT)
```

---

**Configurado com ❤️ por Claude Code**