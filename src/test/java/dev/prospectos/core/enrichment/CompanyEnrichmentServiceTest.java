package dev.prospectos.core.enrichment;

import dev.prospectos.core.domain.Company.CompanySize;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyEnrichmentServiceTest {

    @Mock
    private DataNormalizer dataNormalizer;

    @Mock
    private EmailFilter emailFilter;

    @Mock
    private ContactProcessor contactProcessor;

    private CompanyEnrichmentService enrichmentService;

    @BeforeEach
    void setUp() {
        enrichmentService = new CompanyEnrichmentService(dataNormalizer, emailFilter, contactProcessor);
    }

    @Test
    void enrichCompanyData_ValidRequest_ReturnsEnrichedResult() {
        // Given
        EnrichmentRequest request = new EnrichmentRequest(
            "acme corp",
            "A great company",
            List.of("john@acme.com", "info@acme.com"),
            "555-1234",
            List.of("Java", "Spring"),
            "technology",
            "small",
            List.of("Recent funding"),
            "https://acme.com"
        );

        // Mock normalizer responses
        when(dataNormalizer.normalizeCompanyName("acme corp")).thenReturn("Acme Corp");
        when(dataNormalizer.normalizeDescription("A great company")).thenReturn("A great company");
        when(dataNormalizer.normalizePhone("555-1234")).thenReturn("555-1234");
        when(dataNormalizer.standardizeIndustry("technology")).thenReturn("Technology");
        when(dataNormalizer.mapCompanySize("small")).thenReturn(CompanySize.SMALL);

        // Mock email filter response
        List<ValidatedContact> mockContacts = List.of(
            ValidatedContact.valid(createEmail("john@acme.com"), ValidatedContact.ContactType.CORPORATE)
        );
        when(emailFilter.filterAndValidateEmails(anyList())).thenReturn(mockContacts);

        // When
        EnrichmentResult result = enrichmentService.enrichCompanyData(request);

        // Then
        assertNotNull(result);
        assertEquals("Acme Corp", result.normalizedCompanyName());
        assertEquals("A great company", result.cleanDescription());
        assertEquals("555-1234", result.normalizedPhone());
        assertEquals("Technology", result.standardizedIndustry());
        assertEquals(CompanySize.SMALL, result.size());
        assertEquals(1, result.validatedContacts().size());
        assertNotNull(result.website());
        assertEquals("https://acme.com", result.website().getUrl());
        assertTrue(result.isEnrichmentSuccessful());
    }

    @Test
    void enrichCompanyData_NullRequest_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            enrichmentService.enrichCompanyData(null));
    }

    @Test
    void enrichCompanyData_InvalidWebsite_HandlesGracefully() {
        // Given
        EnrichmentRequest request = new EnrichmentRequest(
            "Acme Corp",
            "Description",
            List.of("john@acme.com"),
            null,
            List.of(),
            "Technology",
            null,
            List.of(),
            "invalid-url"
        );

        // Mock successful normalization
        when(dataNormalizer.normalizeCompanyName(any())).thenReturn("Acme Corp");
        when(dataNormalizer.normalizeDescription(any())).thenReturn("Description");
        when(dataNormalizer.standardizeIndustry(any())).thenReturn("Technology");
        when(emailFilter.filterAndValidateEmails(anyList())).thenReturn(List.of());

        // When
        EnrichmentResult result = enrichmentService.enrichCompanyData(request);

        // Then
        assertNotNull(result);
        assertNull(result.website()); // Invalid website should be null
        assertFalse(result.isEnrichmentSuccessful()); // Should fail due to no website and no contacts
    }

    @Test
    void enrichCompanyData_EmptyEmails_HandlesGracefully() {
        // Given
        EnrichmentRequest request = new EnrichmentRequest(
            "Acme Corp",
            "Description",
            List.of(), // No emails
            null,
            List.of(),
            "Technology",
            null,
            List.of(),
            "https://acme.com"
        );

        // Mock successful normalization
        when(dataNormalizer.normalizeCompanyName(any())).thenReturn("Acme Corp");
        when(dataNormalizer.normalizeDescription(any())).thenReturn("Description");
        when(dataNormalizer.standardizeIndustry(any())).thenReturn("Technology");
        when(emailFilter.filterAndValidateEmails(anyList())).thenReturn(List.of());

        // When
        EnrichmentResult result = enrichmentService.enrichCompanyData(request);

        // Then
        assertNotNull(result);
        assertTrue(result.validatedContacts().isEmpty());
        assertFalse(result.hasValidContacts());
        assertFalse(result.isEnrichmentSuccessful());
    }

    @Test
    void isEnrichmentSufficient_GoodQuality_ReturnsTrue() {
        // Given
        List<ValidatedContact> contacts = List.of(
            ValidatedContact.valid(createEmail("john@acme.com"), ValidatedContact.ContactType.CORPORATE)
        );

        EnrichmentQuality quality = EnrichmentQuality.calculate(1, 1, 1, 0, 0, 0, 5, 6);

        EnrichmentResult result = new EnrichmentResult(
            "Acme Corp",
            "Description",
            contacts,
            "555-1234",
            List.of(),
            "Technology",
            CompanySize.SMALL,
            List.of(),
            createWebsite("https://acme.com"),
            quality
        );

        // When
        boolean sufficient = enrichmentService.isEnrichmentSufficient(result);

        // Then
        assertTrue(sufficient);
    }

    @Test
    void isEnrichmentSufficient_PoorQuality_ReturnsFalse() {
        // Given - No contacts, low quality
        EnrichmentQuality quality = EnrichmentQuality.calculate(1, 0, 0, 0, 0, 1, 2, 6);

        EnrichmentResult result = new EnrichmentResult(
            "Acme Corp",
            "Description",
            List.of(), // No contacts
            null,
            List.of(),
            "Technology",
            null,
            List.of(),
            createWebsite("https://acme.com"),
            quality
        );

        // When
        boolean sufficient = enrichmentService.isEnrichmentSufficient(result);

        // Then
        assertFalse(sufficient);
    }

    @Test
    void getPriorityContacts_DelegatesCorrectly() {
        // Given
        List<ValidatedContact> allContacts = Arrays.asList(
            ValidatedContact.valid(createEmail("john@acme.com"), ValidatedContact.ContactType.CORPORATE),
            ValidatedContact.flagged(createEmail("info@acme.com"), ValidatedContact.ContactType.ROLE_BASED)
        );

        List<ValidatedContact> priorityContacts = List.of(allContacts.get(0));
        when(emailFilter.getPriorityContacts(allContacts)).thenReturn(priorityContacts);

        EnrichmentResult result = new EnrichmentResult(
            "Acme Corp", "Desc", allContacts, null, List.of(),
            "Tech", null, List.of(), null, null
        );

        // When
        List<ValidatedContact> priority = enrichmentService.getPriorityContacts(result);

        // Then
        assertEquals(1, priority.size());
        assertEquals(ValidatedContact.ContactType.CORPORATE, priority.get(0).type());
    }

    private dev.prospectos.core.domain.Email createEmail(String address) {
        return dev.prospectos.core.domain.Email.of(address);
    }

    private dev.prospectos.core.domain.Website createWebsite(String url) {
        return dev.prospectos.core.domain.Website.of(url);
    }
}