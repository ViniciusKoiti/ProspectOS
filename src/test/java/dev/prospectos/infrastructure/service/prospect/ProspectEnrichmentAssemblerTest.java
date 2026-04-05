package dev.prospectos.infrastructure.service.prospect;

import dev.prospectos.core.domain.Company;
import dev.prospectos.core.domain.Website;
import dev.prospectos.core.enrichment.EnrichmentRequest;
import dev.prospectos.core.enrichment.EnrichmentQuality;
import dev.prospectos.core.enrichment.EnrichmentResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProspectEnrichmentAssemblerTest {

    private final ProspectEnrichmentAssembler assembler = new ProspectEnrichmentAssembler();

    @Test
    void mergeWithRequestKeepsProvidedNameAndIndustryWhenPresent() {
        EnrichmentRequest merged = assembler.mergeWithRequest(
            new EnrichmentRequest("Agnes", "desc", List.of(), null, List.of(), "Technology", null, List.of(), "https://agnes.com"),
            "Amalfi Nail Salon",
            "Nail Salon"
        );

        assertThat(merged.companyName()).isEqualTo("Amalfi Nail Salon");
        assertThat(merged.industry()).isEqualTo("Nail Salon");
    }

    @Test
    void buildCompanyKeepsRequestIdentityWhenEnrichmentSuggestsDifferentFields() {
        Company company = assembler.buildCompany(
            "Amalfi Nail Salon",
            "http://www.myagnesapp.com/",
            "Nail Salon",
            new EnrichmentResult(
                "Agnes",
                "desc",
                List.of(),
                null,
                List.of(),
                "Technology",
                null,
                List.of(),
                Website.of("http://www.myagnesapp.com/"),
                EnrichmentQuality.calculate(1, 0, 0, 0, 0, 0, 1, 6)
            )
        );

        assertThat(company.getName()).isEqualTo("Amalfi Nail Salon");
        assertThat(company.getIndustry()).isEqualTo("Nail Salon");
        assertThat(company.getWebsite().getUrl()).isEqualTo("http://www.myagnesapp.com/");
    }
}
