package dev.prospectos.infrastructure.mcp.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.junit.jupiter.api.Test;

import dev.prospectos.api.CompanyDataService;
import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.api.dto.ScoreDTO;
import dev.prospectos.api.mcp.QueryTimeWindow;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultQueryHistoryServiceTest {

    @Test
    void shouldBuildQueryHistoryFromObservedExecutions() {
        var service = service();

        var history = service.getQueryHistory(QueryTimeWindow.ONE_HOUR, "nominatim");

        assertThat(history.timeWindow()).isEqualTo(QueryTimeWindow.ONE_HOUR);
        assertThat(history.provider()).isEqualTo("nominatim");
        assertThat(history.executions()).hasSize(2);
        assertThat(history.executions()).allSatisfy(execution -> assertThat(execution.query()).isNotBlank());
        assertThat(history.aggregatedMetrics()).containsKeys("totalQueries", "successfulQueries", "successRate", "totalCost", "averageResponseTime", "costPerQuery");
    }

    @Test
    void shouldBuildProviderPerformanceFromObservedMetrics() {
        var service = service();

        var performance = service.getProviderPerformance("nominatim", "response_time");

        assertThat(performance.provider()).isEqualTo("nominatim");
        assertThat(performance.metric()).isEqualTo("response_time");
        assertThat(performance.dataPoints()).hasSize(24);
        assertThat(performance.summary()).containsKeys("average", "minimum", "maximum", "trend", "dataPoints");
    }

    @Test
    void shouldBuildMarketAnalysisFromPersistedCompanies() {
        var service = service();

        var analysis = service.getMarketAnalysis("brazil", "technology");

        assertThat(analysis.country()).isEqualTo("brazil");
        assertThat(analysis.industry()).isEqualTo("technology");
        assertThat(analysis.marketMetrics()).containsEntry("marketSize", 2);
        assertThat(analysis.insights()).isNotEmpty();
        assertThat(analysis.competitors()).hasSize(2);
    }

    private DefaultQueryHistoryService service() {
        Instant now = Instant.parse("2026-03-29T12:00:00Z");
        var observations = new ConcurrentLinkedDeque<QueryMetricsObservation>();
        observations.add(new QueryMetricsObservation("nominatim", "tech companies in brazil", now.minusSeconds(600), 900, true, 3, new BigDecimal("0.00")));
        observations.add(new QueryMetricsObservation("nominatim", "tech companies in brazil", now.minusSeconds(1200), 1200, false, 0, new BigDecimal("0.00")));
        observations.add(new QueryMetricsObservation("vector-company", "tech companies in brazil", now.minusSeconds(1800), 140, true, 4, new BigDecimal("0.00")));
        var metricsService = new InMemoryQueryMetricsService(observations, Clock.fixed(now, ZoneOffset.UTC), new QueryMetricsCostEstimator(), new QueryMetricsSnapshotFactory());
        return new DefaultQueryHistoryService(metricsService, companyDataService());
    }

    private CompanyDataService companyDataService() {
        return new CompanyDataService() {
            @Override public CompanyDTO findCompany(Long companyId) { return null; }
            @Override public CompanyDTO findByWebsite(String website) { return null; }
            @Override public java.util.List<CompanyDTO> findAllCompanies() {
                return java.util.List.of(
                    new CompanyDTO(1L, "Alpha Tech", "technology", "https://alpha.test", "", 50, "Sao Paulo, Brazil", new ScoreDTO(80, "HOT", "")),
                    new CompanyDTO(2L, "Beta Cloud", "technology", "https://beta.test", "", 20, "Curitiba, Brazil", new ScoreDTO(70, "WARM", "")),
                    new CompanyDTO(3L, "Gamma Health", "healthcare", "https://gamma.test", "", 15, "Bogota, Colombia", new ScoreDTO(60, "COLD", ""))
                );
            }
            @Override public CompanyDTO createCompany(dev.prospectos.api.dto.request.CompanyCreateRequest request) { throw new UnsupportedOperationException(); }
            @Override public CompanyDTO updateCompany(Long companyId, dev.prospectos.api.dto.request.CompanyUpdateRequest request) { throw new UnsupportedOperationException(); }
            @Override public boolean deleteCompany(Long companyId) { throw new UnsupportedOperationException(); }
            @Override public void updateCompanyScore(Long companyId, ScoreDTO score) { throw new UnsupportedOperationException(); }
            @Override public java.util.List<CompanyDTO> findCompaniesByICP(Long icpId) { return java.util.List.of(); }
            @Override public java.util.List<dev.prospectos.api.dto.CompanyContactDTO> findCompanyContacts(Long companyId) { return java.util.List.of(); }
            @Override public void addCompanyContactEmails(Long companyId, java.util.List<String> emails) { throw new UnsupportedOperationException(); }
        };
    }
}
