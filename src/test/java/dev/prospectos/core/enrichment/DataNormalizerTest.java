package dev.prospectos.core.enrichment;

import dev.prospectos.core.domain.Company.CompanySize;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DataNormalizerTest {

    private DataNormalizer dataNormalizer;

    @BeforeEach
    void setUp() {
        dataNormalizer = new DataNormalizer();
    }

    @Test
    void normalizeCompanyName_TrimsAndCapitalizes() {
        assertEquals("Acme Corporation", dataNormalizer.normalizeCompanyName("  acme corporation  "));
        assertEquals("Tech Startup", dataNormalizer.normalizeCompanyName("tech startup"));
        assertEquals("My Company", dataNormalizer.normalizeCompanyName("MY COMPANY"));
    }

    @Test
    void normalizeCompanyName_RemovesMultipleSpaces() {
        assertEquals("Tech Company", dataNormalizer.normalizeCompanyName("Tech    Company"));
        assertEquals("My Business Group", dataNormalizer.normalizeCompanyName("My  Business   Group"));
    }

    @Test
    void normalizeCompanyName_RemovesCommonSuffixes() {
        assertEquals("Acme Corporation", dataNormalizer.normalizeCompanyName("Acme Corporation Inc."));
        assertEquals("Tech Solutions", dataNormalizer.normalizeCompanyName("Tech Solutions LLC"));
        assertEquals("Business Services", dataNormalizer.normalizeCompanyName("Business Services Ltd."));
    }

    @Test
    void normalizeCompanyName_HandlesNullAndEmpty() {
        assertNull(dataNormalizer.normalizeCompanyName(null));
        assertNull(dataNormalizer.normalizeCompanyName(""));
        assertNull(dataNormalizer.normalizeCompanyName("   "));
    }

    @Test
    void normalizeDescription_CleansAndTrims() {
        String input = "  This is a   company description with    extra spaces.  ";
        String expected = "This is a company description with extra spaces.";
        assertEquals(expected, dataNormalizer.normalizeDescription(input));
    }

    @Test
    void normalizeDescription_TruncatesLongText() {
        String longText = "A".repeat(600);
        String result = dataNormalizer.normalizeDescription(longText);

        assertTrue(result.length() <= 500);
        assertTrue(result.endsWith("..."));
    }

    @Test
    void normalizeDescription_HandlesNullAndEmpty() {
        assertNull(dataNormalizer.normalizeDescription(null));
        assertNull(dataNormalizer.normalizeDescription(""));
        assertNull(dataNormalizer.normalizeDescription("   "));
    }

    @Test
    void standardizeIndustry_MapsKnownIndustries() {
        assertEquals("Technology", dataNormalizer.standardizeIndustry("tech"));
        assertEquals("Technology", dataNormalizer.standardizeIndustry("software"));
        assertEquals("Financial Services", dataNormalizer.standardizeIndustry("finance"));
        assertEquals("Financial Services", dataNormalizer.standardizeIndustry("fintech"));
        assertEquals("Healthcare", dataNormalizer.standardizeIndustry("healthcare"));
        assertEquals("Retail", dataNormalizer.standardizeIndustry("retail"));
        assertEquals("Retail", dataNormalizer.standardizeIndustry("e-commerce"));
    }

    @Test
    void standardizeIndustry_HandlesPartialMatches() {
        assertEquals("Technology", dataNormalizer.standardizeIndustry("software development"));
        assertEquals("Financial Services", dataNormalizer.standardizeIndustry("financial technology"));
    }

    @Test
    void standardizeIndustry_CapitalizesUnknownIndustries() {
        assertEquals("Custom Industry", dataNormalizer.standardizeIndustry("custom industry"));
        assertEquals("Unknown Business", dataNormalizer.standardizeIndustry("unknown business"));
    }

    @Test
    void standardizeIndustry_HandlesNullAndEmpty() {
        assertEquals("Other", dataNormalizer.standardizeIndustry(null));
        assertEquals("Other", dataNormalizer.standardizeIndustry(""));
        assertEquals("Other", dataNormalizer.standardizeIndustry("   "));
    }

    @Test
    void normalizePhone_CleansFormat() {
        assertEquals("+1-555-123-4567", dataNormalizer.normalizePhone("+1-555-123-4567"));
        assertEquals("(555) 123-4567", dataNormalizer.normalizePhone("(555) 123-4567"));
        assertEquals("555.123.4567", dataNormalizer.normalizePhone("555.123.4567"));
    }

    @Test
    void normalizePhone_RejectsInvalidPhones() {
        assertNull(dataNormalizer.normalizePhone("abc"));
        assertNull(dataNormalizer.normalizePhone("123")); // Too short
        assertNull(dataNormalizer.normalizePhone("notaphone"));
    }

    @Test
    void normalizePhone_HandlesNullAndEmpty() {
        assertNull(dataNormalizer.normalizePhone(null));
        assertNull(dataNormalizer.normalizePhone(""));
        assertNull(dataNormalizer.normalizePhone("   "));
    }

    @Test
    void mapCompanySize_MapsKnownSizes() {
        assertEquals(CompanySize.STARTUP, dataNormalizer.mapCompanySize("startup"));
        assertEquals(CompanySize.SMALL, dataNormalizer.mapCompanySize("small"));
        assertEquals(CompanySize.MEDIUM, dataNormalizer.mapCompanySize("medium"));
        assertEquals(CompanySize.LARGE, dataNormalizer.mapCompanySize("large"));
        assertEquals(CompanySize.ENTERPRISE, dataNormalizer.mapCompanySize("enterprise"));
    }

    @Test
    void mapCompanySize_MapsEmployeeRanges() {
        assertEquals(CompanySize.STARTUP, dataNormalizer.mapCompanySize("1-10"));
        assertEquals(CompanySize.SMALL, dataNormalizer.mapCompanySize("11-50"));
        assertEquals(CompanySize.MEDIUM, dataNormalizer.mapCompanySize("51-200"));
        assertEquals(CompanySize.LARGE, dataNormalizer.mapCompanySize("201-1000"));
        assertEquals(CompanySize.ENTERPRISE, dataNormalizer.mapCompanySize("1000+"));
    }

    @Test
    void mapCompanySize_InfersFromText() {
        assertEquals(CompanySize.SMALL, dataNormalizer.mapCompanySize("small business"));
        assertEquals(CompanySize.LARGE, dataNormalizer.mapCompanySize("large corporation"));
        assertEquals(CompanySize.ENTERPRISE, dataNormalizer.mapCompanySize("enterprise level"));
    }

    @Test
    void mapCompanySize_HandlesEmployeeNumbers() {
        assertEquals(CompanySize.STARTUP, dataNormalizer.mapCompanySize("5-15 employees"));
        assertEquals(CompanySize.SMALL, dataNormalizer.mapCompanySize("25-45 employees"));
        assertEquals(CompanySize.MEDIUM, dataNormalizer.mapCompanySize("100-150 employees"));
        assertEquals(CompanySize.LARGE, dataNormalizer.mapCompanySize("500-800 employees"));
        assertEquals(CompanySize.ENTERPRISE, dataNormalizer.mapCompanySize("2000+ employees"));
    }

    @Test
    void mapCompanySize_HandlesNullAndUnknown() {
        assertNull(dataNormalizer.mapCompanySize(null));
        assertNull(dataNormalizer.mapCompanySize(""));
        assertNull(dataNormalizer.mapCompanySize("unknown"));
        assertNull(dataNormalizer.mapCompanySize("varies"));
    }
}
