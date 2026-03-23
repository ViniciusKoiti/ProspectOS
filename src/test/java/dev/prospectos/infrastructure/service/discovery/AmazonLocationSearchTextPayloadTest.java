package dev.prospectos.infrastructure.service.discovery;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AmazonLocationSearchTextPayloadTest {

    @Test
    void requestPayloadShouldExposeConfiguredFields() {
        AmazonLocationSearchTextFilter filter = new AmazonLocationSearchTextFilter(List.of("BRA", "USA"));
        AmazonLocationSearchTextRequest request = new AmazonLocationSearchTextRequest(
            "cloud companies",
            "pt-BR",
            "SingleUse",
            20,
            filter
        );

        assertThat(request.queryText()).isEqualTo("cloud companies");
        assertThat(request.language()).isEqualTo("pt-BR");
        assertThat(request.intendedUse()).isEqualTo("SingleUse");
        assertThat(request.maxResults()).isEqualTo(20);
        assertThat(request.filter().includeCountries()).containsExactly("BRA", "USA");
    }

    @Test
    void responsePayloadShouldExposeNestedFields() {
        AmazonLocationSubRegion subRegion = new AmazonLocationSubRegion("Ontario");
        AmazonLocationCountry country = new AmazonLocationCountry("CA", "CAN", "Canada");
        AmazonLocationAddress address = new AmazonLocationAddress(
            "Toronto, ON, Canada",
            null,
            null,
            subRegion,
            country
        );
        AmazonLocationCategory category = new AmazonLocationCategory("Business Facility");
        AmazonLocationContactValue email = new AmazonLocationContactValue("hello@example.com");
        AmazonLocationContactValue website = new AmazonLocationContactValue("example.com");
        AmazonLocationContacts contacts = new AmazonLocationContacts(List.of(email), List.of(website));
        AmazonLocationResultItem item = new AmazonLocationResultItem(
            "Example Office",
            "PointOfInterest",
            address,
            List.of(category),
            contacts
        );
        AmazonLocationSearchTextResponse response = new AmazonLocationSearchTextResponse(List.of(item));

        assertThat(subRegion.name()).isEqualTo("Ontario");
        assertThat(country.code2()).isEqualTo("CA");
        assertThat(country.code3()).isEqualTo("CAN");
        assertThat(country.name()).isEqualTo("Canada");
        assertThat(address.subRegion().name()).isEqualTo("Ontario");
        assertThat(address.country().code2()).isEqualTo("CA");
        assertThat(category.name()).isEqualTo("Business Facility");
        assertThat(email.value()).isEqualTo("hello@example.com");
        assertThat(website.value()).isEqualTo("example.com");
        assertThat(response.resultItems()).hasSize(1);
        assertThat(response.resultItems().getFirst().title()).isEqualTo("Example Office");
        assertThat(response.resultItems().getFirst().contacts().emails()).containsExactly(email);
    }
}
