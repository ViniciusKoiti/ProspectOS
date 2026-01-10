package dev.prospectos.core.enrichment;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ScraperDataMapperTest {

    @Test
    void fromScraperData_MapsFieldsAndLists() {
        Map<String, Object> data = Map.of(
            "company_name", "Acme Corp",
            "description", "A company",
            "emails", List.of("sales@acme.com", "info@acme.com"),
            "phone", "555-1234",
            "technologies", List.of("Java", "Spring"),
            "industry", "technology",
            "size", "small",
            "recent_news", List.of("Funding round")
        );

        EnrichmentRequest request = ScraperDataMapper.fromScraperData(data, "https://acme.com");

        assertEquals("Acme Corp", request.companyName());
        assertEquals("A company", request.description());
        assertEquals(List.of("sales@acme.com", "info@acme.com"), request.emails());
        assertEquals("555-1234", request.phone());
        assertEquals(List.of("Java", "Spring"), request.technologies());
        assertEquals("technology", request.industry());
        assertEquals("small", request.size());
        assertEquals(List.of("Funding round"), request.recentNews());
        assertEquals("https://acme.com", request.website());
    }

    @Test
    void fromScraperData_WrapsSingleStringValues() {
        Map<String, Object> data = Map.of(
            "emails", "info@acme.com",
            "technologies", "Java"
        );

        EnrichmentRequest request = ScraperDataMapper.fromScraperData(data, "https://acme.com");

        assertEquals(List.of("info@acme.com"), request.emails());
        assertEquals(List.of("Java"), request.technologies());
    }
}
