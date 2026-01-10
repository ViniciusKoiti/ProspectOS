package dev.prospectos.infrastructure.service.compliance;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AllowedSourcesComplianceServiceTest {

    @Test
    void allowsRequestedSourcesWhenAllAllowed() {
        AllowedSourcesComplianceService service = new AllowedSourcesComplianceService(
            List.of("google", "linkedin")
        );

        List<String> result = service.validateSources(List.of("google"));

        assertThat(result).containsExactly("google");
    }

    @Test
    void rejectsDisallowedSources() {
        AllowedSourcesComplianceService service = new AllowedSourcesComplianceService(
            List.of("google")
        );

        assertThatThrownBy(() -> service.validateSources(List.of("zoominfo")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Disallowed source");
    }

    @Test
    void ignoresBlankAndNullSources() {
        AllowedSourcesComplianceService service = new AllowedSourcesComplianceService(
            List.of("google")
        );

        List<String> result = service.validateSources(Arrays.asList(null, " ", "google"));

        assertThat(result).containsExactly("google");
    }

    @Test
    void normalizesAndDeduplicatesSources() {
        AllowedSourcesComplianceService service = new AllowedSourcesComplianceService(
            List.of("LinkedIn", "Google")
        );

        List<String> result = service.validateSources(List.of(" linkedin ", "GOOGLE", "google"));

        assertThat(result).containsExactly("linkedin", "google");
    }

    @Test
    void rejectsWhenNoAllowedSourcesConfigured() {
        AllowedSourcesComplianceService service = new AllowedSourcesComplianceService(List.of());

        assertThatThrownBy(() -> service.validateSources(List.of("google")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("No allowed sources configured");
    }
}
