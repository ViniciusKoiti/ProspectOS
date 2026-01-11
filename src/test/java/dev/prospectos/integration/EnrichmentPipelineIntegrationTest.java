package dev.prospectos.integration;

import dev.prospectos.core.enrichment.CompanyEnrichmentService;
import dev.prospectos.core.enrichment.EnrichmentRequest;
import dev.prospectos.core.enrichment.EnrichmentResult;
import dev.prospectos.core.enrichment.ValidatedContact;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration test for the complete enrichment pipeline.
 * Tests the MVP-004 enrichment and email validation implementation.
 */
@SpringBootTest
@TestPropertySource(locations = "file:.env")
@ActiveProfiles("test")
class EnrichmentPipelineIntegrationTest {

    @Autowired
    private CompanyEnrichmentService enrichmentService;

    @Test
    void enrichmentPipeline_ProcessesValidData_ReturnsQualityResult() {
        // Given: Raw company data with mixed email quality
        EnrichmentRequest request = new EnrichmentRequest(
            "  acme corporation inc.  ", // Needs normalization
            "  This is a great   company with    extra spaces.  ", // Needs cleaning
            Arrays.asList(
                "john.doe@acme.com",      // Valid corporate
                "info@acme.com",          // Role-based
                "user@gmail.com",         // Personal
                "invalid-email",          // Invalid
                "support@acme.com",       // Support
                "john.doe@acme.com"       // Duplicate
            ),
            "+1-555-123-4567",
            List.of("Java", "Spring", "React"),
            "software development",
            "small business",
            List.of("Recent funding round", "Product launch"),
            "https://acme.com"
        );

        // When: Processing through enrichment pipeline
        EnrichmentResult result = enrichmentService.enrichCompanyData(request);

        // Then: Verify normalization
        assertThat(result.normalizedCompanyName()).isEqualTo("Acme Corporation");
        assertThat(result.cleanDescription()).isEqualTo("This is a great company with extra spaces.");
        assertThat(result.standardizedIndustry()).isEqualTo("Technology");
        assertThat(result.size()).isEqualTo(dev.prospectos.core.domain.Company.CompanySize.SMALL);

        // Then: Verify website processing
        assertThat(result.website()).isNotNull();
        assertThat(result.website().getUrl()).isEqualTo("https://acme.com");

        // Then: Verify email filtering and validation
        assertThat(result.validatedContacts()).hasSize(5); // Removed invalid and duplicate

        long corporateEmails = result.validatedContacts().stream()
            .filter(c -> c.type() == ValidatedContact.ContactType.CORPORATE)
            .count();
        assertThat(corporateEmails).isEqualTo(1); // john.doe@acme.com

        long roleBasedEmails = result.validatedContacts().stream()
            .filter(c -> c.type() == ValidatedContact.ContactType.ROLE_BASED)
            .count();
        assertThat(roleBasedEmails).isEqualTo(1); // info@acme.com

        long supportEmails = result.validatedContacts().stream()
            .filter(c -> c.type() == ValidatedContact.ContactType.SUPPORT)
            .count();
        assertThat(supportEmails).isEqualTo(1); // support@acme.com

        long personalEmails = result.validatedContacts().stream()
            .filter(c -> c.type() == ValidatedContact.ContactType.PERSONAL)
            .count();
        assertThat(personalEmails).isEqualTo(1); // user@gmail.com

        // Then: Verify quality metrics
        assertThat(result.quality().totalEmailsProcessed()).isEqualTo(6);
        assertThat(result.quality().validEmailsFound()).isEqualTo(5);
        assertThat(result.quality().invalidEmailsFiltered()).isEqualTo(1);
        assertThat(result.quality().completenessScore()).isGreaterThan(0.8);

        // Then: Verify enrichment success
        assertThat(result.isEnrichmentSuccessful()).isTrue();
        assertThat(result.hasValidContacts()).isTrue();
        assertThat(enrichmentService.isEnrichmentSufficient(result)).isTrue();
    }

    @Test
    void enrichmentPipeline_HandlesMinimalData_DoesNotFail() {
        // Given: Minimal company data
        EnrichmentRequest minimalRequest = new EnrichmentRequest(
            "Minimal Co",
            null,
            List.of(),
            null,
            List.of(),
            null,
            null,
            List.of(),
            "https://minimal.co"
        );

        // When: Processing minimal data
        EnrichmentResult result = enrichmentService.enrichCompanyData(minimalRequest);

        // Then: Should complete without errors
        assertThat(result).isNotNull();
        assertThat(result.normalizedCompanyName()).isEqualTo("Minimal Co");
        assertThat(result.website()).isNotNull();
        assertThat(result.validatedContacts()).isEmpty();
        assertThat(result.hasValidContacts()).isFalse();
        assertThat(result.isEnrichmentSuccessful()).isFalse(); // No contacts
    }

    @Test
    void enrichmentPipeline_HandlesInvalidWebsite_ReturnsNullWebsite() {
        // Given: Data with invalid website
        EnrichmentRequest request = new EnrichmentRequest(
            "Test Company",
            "Test description",
            List.of("test@test.com"),
            null,
            List.of(),
            "Technology",
            null,
            List.of(),
            "not-a-valid-url"
        );

        // When: Processing with invalid website
        EnrichmentResult result = enrichmentService.enrichCompanyData(request);

        // Then: Should handle gracefully
        assertThat(result).isNotNull();
        assertThat(result.website()).isNull(); // Invalid URL becomes null
        assertThat(result.normalizedCompanyName()).isEqualTo("Test Company");
        assertThat(result.validatedContacts()).hasSize(1);
        assertThat(result.isEnrichmentSuccessful()).isFalse(); // No valid website
    }

    @Test
    void enrichmentPipeline_HandlesAllInvalidEmails_ReturnsEmptyContacts() {
        // Given: Data with only invalid emails
        EnrichmentRequest request = new EnrichmentRequest(
            "Test Company",
            "Test description",
            List.of("invalid", "also-invalid", "@invalid.com", "invalid@"),
            null,
            List.of(),
            "Technology",
            null,
            List.of(),
            "https://test.com"
        );

        // When: Processing with all invalid emails
        EnrichmentResult result = enrichmentService.enrichCompanyData(request);

        // Then: Should handle gracefully
        assertThat(result).isNotNull();
        assertThat(result.validatedContacts()).isEmpty();
        assertThat(result.quality().totalEmailsProcessed()).isEqualTo(4);
        assertThat(result.quality().validEmailsFound()).isEqualTo(0);
        assertThat(result.quality().invalidEmailsFiltered()).isEqualTo(4);
        assertThat(result.isEnrichmentSuccessful()).isFalse(); // No valid contacts
    }

    @Test
    void enrichmentPipeline_GetsPriorityContacts_ReturnsOnlyCorporate() {
        // Given: Mixed contact types
        EnrichmentRequest request = new EnrichmentRequest(
            "Test Company",
            "Description",
            List.of(
                "john@company.com",    // Corporate - priority
                "info@company.com",    // Role-based - not priority
                "user@gmail.com"       // Personal - not priority
            ),
            null,
            List.of(),
            "Technology",
            null,
            List.of(),
            "https://company.com"
        );

        // When: Processing and getting priority contacts
        EnrichmentResult result = enrichmentService.enrichCompanyData(request);
        List<ValidatedContact> priorityContacts = enrichmentService.getPriorityContacts(result);

        // Then: Only corporate contacts should be priority
        assertThat(priorityContacts).hasSize(1);
        assertThat(priorityContacts.get(0).email().getAddress()).isEqualTo("john@company.com");
        assertThat(priorityContacts.get(0).type()).isEqualTo(ValidatedContact.ContactType.CORPORATE);
    }
}