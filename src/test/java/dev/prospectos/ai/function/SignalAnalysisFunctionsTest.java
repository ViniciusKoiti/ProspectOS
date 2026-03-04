package dev.prospectos.ai.function;

import dev.prospectos.core.domain.Company;
import dev.prospectos.core.domain.Website;
import dev.prospectos.core.domain.events.SignalDetected;
import dev.prospectos.core.repository.CompanyDomainRepository;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SignalAnalysisFunctionsTest {

    @Test
    void analyzeCompanySignalsBuildsSignalSummary() {
        Company company = Company.create("Acme", Website.of("https://acme.com"), "Software");
        company.detectSignal(
            SignalDetected.SignalType.TECHNOLOGY_ADOPTION,
            "Using Kubernetes",
            Map.of("technology", "Kubernetes")
        );
        CompanyDomainRepository repository = mock(CompanyDomainRepository.class);
        when(repository.findById(company.getId())).thenReturn(Optional.of(company));
        SignalAnalysisFunctions functions = new SignalAnalysisFunctions(repository);

        Map<String, Object> result = functions.analyzeCompanySignals().apply(
            new SignalAnalysisFunctions.SignalRequest(company.getId())
        );

        assertThat(result.get("totalSignals")).isEqualTo(1);
        assertThat(result.get("hasActiveSignals")).isEqualTo(true);
        assertThat(result.get("mostRecentSignal")).asString().contains("Kubernetes");
    }

    @Test
    void analyzeCompanySignalsFailsWhenCompanyDoesNotExist() {
        UUID id = UUID.randomUUID();
        CompanyDomainRepository repository = mock(CompanyDomainRepository.class);
        when(repository.findById(id)).thenReturn(Optional.empty());
        SignalAnalysisFunctions functions = new SignalAnalysisFunctions(repository);

        assertThatThrownBy(() -> functions.analyzeCompanySignals().apply(new SignalAnalysisFunctions.SignalRequest(id)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Company not found");
    }
}
