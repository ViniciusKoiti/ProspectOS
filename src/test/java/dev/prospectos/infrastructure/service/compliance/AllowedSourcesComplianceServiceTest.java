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
            List.of("google", "linkedin"),
            List.of("google")
        );

        List<String> result = service.validateSources(List.of("google"));

        assertThat(result).containsExactly("google");
    }

    @Test
    void rejectsDisallowedSources() {
        AllowedSourcesComplianceService service = new AllowedSourcesComplianceService(
            List.of("google"),
            List.of("google")
        );

        assertThatThrownBy(() -> service.validateSources(List.of("zoominfo")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Disallowed source");
    }

    @Test
    void ignoresBlankAndNullSources() {
        AllowedSourcesComplianceService service = new AllowedSourcesComplianceService(
            List.of("google"),
            List.of("google")
        );

        List<String> result = service.validateSources(Arrays.asList(null, " ", "google"));

        assertThat(result).containsExactly("google");
    }

    @Test
    void normalizesAndDeduplicatesSources() {
        AllowedSourcesComplianceService service = new AllowedSourcesComplianceService(
            List.of("LinkedIn", "Google"),
            List.of("google")
        );

        List<String> result = service.validateSources(List.of(" linkedin ", "GOOGLE", "google"));

        assertThat(result).containsExactly("linkedin", "google");
    }

    @Test
    void rejectsWhenNoAllowedSourcesConfigured() {
        AllowedSourcesComplianceService service = new AllowedSourcesComplianceService(List.of(), List.of());

        assertThatThrownBy(() -> service.validateSources(List.of("google")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("No allowed sources configured");
    }

    @Test
    void usesDefaultSourcesWhenRequestSourcesAreMissing() {
        AllowedSourcesComplianceService service = new AllowedSourcesComplianceService(
            List.of("in-memory", "scraper"),
            List.of("in-memory")
        );

        List<String> result = service.validateSources(null);

        assertThat(result).containsExactly("in-memory");
    }

    @Test
    void usesDefaultSourcesWhenRequestSourcesAreBlank() {
        AllowedSourcesComplianceService service = new AllowedSourcesComplianceService(
            List.of("in-memory", "scraper"),
            List.of("scraper")
        );

        List<String> result = service.validateSources(Arrays.asList(" ", null));

        assertThat(result).containsExactly("scraper");
    }
}
