package dev.prospectos.infrastructure.service.discovery;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AmazonLocationFieldExtractorTest {

    private final AmazonLocationFieldExtractor fieldExtractor = new AmazonLocationFieldExtractor();

    @Test
    void resolveLocationUsesSubRegionWhenLocalityAndDistrictAreMissing() {
        AmazonLocationAddress address = new AmazonLocationAddress(
            "Amazon Fulfillment Center, Ontario, Canada",
            null,
            null,
            new AmazonLocationSubRegion("Ontario"),
            new AmazonLocationCountry("CA", "CAN", "Canada")
        );

        assertThat(fieldExtractor.resolveLocation(address)).isEqualTo("Ontario, CA");
    }
}
