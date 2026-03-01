package dev.prospectos;

import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * 🧪 TEST SUITE COMPLETO - MVP WEEK 1
 *
 * Suite de testes automatizados que valida:
 * ✅ Funcionalidade completa do sistema
 * ✅ Integração entre componentes
 * ✅ Performance básica
 * ✅ Qualidade dos dados
 * ✅ Cenários de demonstração
 *
 * Para executar: ./gradlew test --tests "dev.prospectos.Week1TestSuite"
 */
@Suite
@SuiteDisplayName("🚀 ProspectOS MVP Week 1 - Complete Test Suite")
@SelectPackages({
    "dev.prospectos.infrastructure.config",
    "dev.prospectos.infrastructure.service.discovery",
    "dev.prospectos.integration"
})
@IncludeClassNamePatterns({
    ".*DataSeederTest",
    ".*CNPJLeadDiscoverySourceTest",
    ".*Week1MVPIntegrationTest"
})
public class Week1TestSuite {
}
