#!/bin/bash

# 🧪 Execução Automática de Testes - MVP Week 1
# Execute este script para rodar toda a suite de testes automatizados

set -e

echo "🚀 ProspectOS MVP Week 1 - Automated Test Suite"
echo "==============================================="

# Cores para output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}📋 Running automated tests...${NC}"

# 1. Executar testes unitários específicos
echo -e "\n${YELLOW}🔬 UNIT TESTS${NC}"
echo "=============="

echo "Testing DataSeeder..."
./gradlew test --tests "dev.prospectos.infrastructure.config.DataSeederTest" --info

echo "Testing CNPJ Integration..."  
./gradlew test --tests "dev.prospectos.infrastructure.service.discovery.CNPJLeadDiscoverySourceTest" --info

# 2. Executar testes de integração
echo -e "\n${YELLOW}🔗 INTEGRATION TESTS${NC}"
echo "==================="

echo "Testing complete MVP Week 1 integration..."
./gradlew test --tests "dev.prospectos.integration.Week1MVPIntegrationTest" --info

# 3. Executar suite completa
echo -e "\n${YELLOW}🎯 COMPLETE TEST SUITE${NC}"
echo "======================"

echo "Running complete Week 1 test suite..."
./gradlew test --tests "dev.prospectos.Week1TestSuite" --info

# 4. Gerar relatório de cobertura
echo -e "\n${YELLOW}📊 COVERAGE REPORT${NC}"
echo "=================="

./gradlew jacocoTestReport

# 5. Verificar resultados
echo -e "\n${BLUE}📋 Test Results Summary${NC}"
echo "======================="

# Verificar se há falhas nos testes
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ ALL TESTS PASSED!${NC}"
    echo ""
    echo -e "${GREEN}🎉 MVP Week 1 is ready for production!${NC}"
    echo ""
    echo "📄 Coverage report: build/reports/jacoco/test/html/index.html"
    echo "📊 Test reports: build/reports/tests/test/index.html"
else
    echo -e "${RED}❌ SOME TESTS FAILED!${NC}"
    echo ""
    echo -e "${RED}Please check the test output above for details.${NC}"
    exit 1
fi

# 6. Testes de performance básicos
echo -e "\n${YELLOW}⚡ PERFORMANCE VALIDATION${NC}"
echo "========================"

echo "Starting application for performance tests..."
./gradlew bootRun --args="--spring.profiles.active=test" &
APP_PID=$!

# Aguardar startup
sleep 15

# Teste básico de performance  
echo "Testing endpoint response times..."
time curl -s http://localhost:8080/api/companies > /dev/null
time curl -s -X POST http://localhost:8080/api/leads/search \
  -H "Content-Type: application/json" \
  -d '{"query": "test", "limit": 5}' > /dev/null

# Cleanup
kill $APP_PID 2>/dev/null || true

echo -e "\n${GREEN}🎯 Automated testing complete!${NC}"
echo ""
echo "Next steps:"
echo "1. Review test reports in build/reports/"
echo "2. Check coverage report for any gaps"  
echo "3. Run manual demo scenarios if needed"
echo "4. Deploy to staging environment"