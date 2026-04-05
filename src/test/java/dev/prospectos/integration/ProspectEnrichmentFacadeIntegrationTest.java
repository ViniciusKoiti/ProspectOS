package dev.prospectos.integration;

import dev.prospectos.ai.client.ScraperClientInterface;
import dev.prospectos.ai.client.ScrapingResponse;
import dev.prospectos.api.ProspectEnrichService;
import dev.prospectos.api.dto.ProspectContactResponse;
import dev.prospectos.api.dto.ProspectEnrichRequest;
import dev.prospectos.api.dto.ProspectEnrichResponse;
import dev.prospectos.core.domain.Company;
import dev.prospectos.core.domain.CompanySize;
import dev.prospectos.core.domain.Email;
import dev.prospectos.core.domain.Website;
import dev.prospectos.core.enrichment.CompanyEnrichmentService;
import dev.prospectos.core.enrichment.ContactProcessor;
import dev.prospectos.core.enrichment.EnrichmentQuality;
import dev.prospectos.core.enrichment.EnrichmentResult;
import dev.prospectos.core.enrichment.ValidatedContact;
import dev.prospectos.infrastructure.service.prospect.HunterContactEnrichmentProvider;
import dev.prospectos.infrastructure.service.prospect.PageSpeedAuditProvider;
import dev.prospectos.infrastructure.service.prospect.PageSpeedAuditResult;
import dev.prospectos.infrastructure.service.prospect.ProspectEnrichmentFacade;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = {
    ProspectEnrichmentFacade.class,
    ContactProcessor.class
})
class ProspectEnrichmentFacadeIntegrationTest {

    @Autowired
    private ProspectEnrichmentFacade facade;

    @MockitoBean
    private ProspectEnrichService prospectEnrichService;

    @MockitoBean
    private ScraperClientInterface scraperClient;

    @MockitoBean
    private CompanyEnrichmentService companyEnrichmentService;

    @MockitoBean
    private PageSpeedAuditProvider pageSpeedAuditProvider;

    @MockitoBean
    private HunterContactEnrichmentProvider hunterContactEnrichmentProvider;

    @Test
    void enrichNormalizesWebsiteAndAppliesScraperBasedEnrichment() {
        given(scraperClient.scrapeWebsiteSync("https://acme.com", false)).willReturn(new ScrapingResponse(
            true,
            Map.of(
                "company_name", "Acme Raw",
                "industry", "Raw Industry",
                "emails", List.of("ceo@acme.com")
            ),
            null
        ));
        given(companyEnrichmentService.enrichCompanyData(any())).willReturn(new EnrichmentResult(
            "Acme Corp",
            "Clean description",
            List.of(ValidatedContact.valid(
                Email.of("ceo@acme.com"),
                ValidatedContact.ContactType.CORPORATE
            )),
            null,
            List.of("Java"),
            "SaaS",
            CompanySize.MEDIUM,
            List.of("Raised Series A"),
            Website.of("https://acme.com"),
            EnrichmentQuality.calculate(1, 1, 1, 0, 0, 0, 5, 6)
        ));
        given(prospectEnrichService.enrichCompany(any(Company.class))).willReturn("Strong fit");
        given(pageSpeedAuditProvider.audit("https://acme.com")).willReturn(new PageSpeedAuditResult(
            40,
            List.of("PageSpeed indicates poor technical performance on the mobile audit.")
        ));
        given(hunterContactEnrichmentProvider.findContacts("https://acme.com")).willReturn(List.of(
            new ProspectContactResponse("owner@acme.com", "Owner Acme", "Founder", 91, "hunter")
        ));

        ProspectEnrichResponse response = facade.enrich(new ProspectEnrichRequest(
            "Acme",
            "acme.com",
            "Technology"
        ));

        ArgumentCaptor<Company> companyCaptor = ArgumentCaptor.forClass(Company.class);
        verify(prospectEnrichService).enrichCompany(companyCaptor.capture());

        Company enrichedCompany = companyCaptor.getValue();
        assertThat(enrichedCompany.getName()).isEqualTo("Acme");
        assertThat(enrichedCompany.getWebsite().getUrl()).isEqualTo("https://acme.com");
        assertThat(enrichedCompany.getIndustry()).isEqualTo("Technology");
        assertThat(enrichedCompany.getDescription()).isEqualTo("Clean description");
        assertThat(enrichedCompany.getSize()).isEqualTo(CompanySize.MEDIUM);
        assertThat(enrichedCompany.getContacts()).hasSize(1);
        assertThat(enrichedCompany.getContacts().getFirst().getEmail().getAddress()).isEqualTo("ceo@acme.com");

        assertThat(response.name()).isEqualTo("Acme");
        assertThat(response.website()).isEqualTo("https://acme.com");
        assertThat(response.industry()).isEqualTo("Technology");
        assertThat(response.analysis()).isEqualTo("Strong fit");
        assertThat(response.audit()).isNotNull();
        assertThat(response.audit().status()).isEqualTo("REVIEW");
        assertThat(response.audit().score()).isEqualTo(60);
        assertThat(response.audit().contactInfoDetected()).isTrue();
        assertThat(response.audit().pageSpeedScore()).isEqualTo(40);
        assertThat(response.contacts()).hasSize(2);
        assertThat(response.contacts().getFirst().email()).isEqualTo("owner@acme.com");
        assertThat(response.contacts().get(1).email()).isEqualTo("ceo@acme.com");

        verify(scraperClient).scrapeWebsiteSync("https://acme.com", false);
        verify(pageSpeedAuditProvider).audit("https://acme.com");
        verify(hunterContactEnrichmentProvider).findContacts("https://acme.com");
    }

    @Test
    void enrichFallsBackToRequestDataWhenScraperFails() {
        given(scraperClient.scrapeWebsiteSync("https://fallback.example.com", false)).willReturn(new ScrapingResponse(
            false,
            null,
            "timeout"
        ));
        given(prospectEnrichService.enrichCompany(any(Company.class))).willReturn("Fallback analysis");
        given(pageSpeedAuditProvider.audit("https://fallback.example.com")).willReturn(PageSpeedAuditResult.unavailable());
        given(hunterContactEnrichmentProvider.findContacts("https://fallback.example.com")).willReturn(List.of());

        ProspectEnrichResponse response = facade.enrich(new ProspectEnrichRequest(
            "Fallback Co",
            "fallback.example.com",
            "Retail"
        ));

        ArgumentCaptor<Company> companyCaptor = ArgumentCaptor.forClass(Company.class);
        verify(prospectEnrichService).enrichCompany(companyCaptor.capture());

        Company company = companyCaptor.getValue();
        assertThat(company.getName()).isEqualTo("Fallback Co");
        assertThat(company.getWebsite().getUrl()).isEqualTo("https://fallback.example.com");
        assertThat(company.getIndustry()).isEqualTo("Retail");
        assertThat(company.getDescription()).isNull();
        assertThat(company.getContacts()).isEmpty();

        assertThat(response.name()).isEqualTo("Fallback Co");
        assertThat(response.website()).isEqualTo("https://fallback.example.com");
        assertThat(response.industry()).isEqualTo("Retail");
        assertThat(response.analysis()).isEqualTo("Fallback analysis");
        assertThat(response.audit()).isNotNull();
        assertThat(response.audit().status()).isEqualTo("REVIEW");
        assertThat(response.audit().scrapeSucceeded()).isFalse();
        assertThat(response.audit().pageSpeedScore()).isNull();
        assertThat(response.contacts()).isEmpty();
    }

    @Test
    void enrichRejectsNullRequest() {
        assertThatThrownBy(() -> facade.enrich(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Request cannot be null");
    }

    @Test
    void enrichRejectsBlankNameOrWebsite() {
        assertThatThrownBy(() -> facade.enrich(new ProspectEnrichRequest(" ", " ", null)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Name and website are required");

        verify(scraperClient, never()).scrapeWebsiteSync(any(), any(Boolean.class));
    }

    @Test
    void enrichKeepsExistingHttpSchemeFromRequest() {
        given(scraperClient.scrapeWebsiteSync("http://legacy.example.com", false)).willReturn(new ScrapingResponse(
            false,
            null,
            "timeout"
        ));
        given(prospectEnrichService.enrichCompany(any(Company.class))).willReturn("Legacy analysis");
        given(pageSpeedAuditProvider.audit("http://legacy.example.com")).willReturn(PageSpeedAuditResult.unavailable());
        given(hunterContactEnrichmentProvider.findContacts("http://legacy.example.com")).willReturn(List.of());

        ProspectEnrichResponse response = facade.enrich(new ProspectEnrichRequest(
            "Legacy Co",
            "http://legacy.example.com",
            "Services"
        ));

        assertThat(response.website()).isEqualTo("http://legacy.example.com");
        assertThat(response.audit()).isNotNull();
        assertThat(response.audit().secure()).isFalse();
        verify(scraperClient).scrapeWebsiteSync("http://legacy.example.com", false);
    }
}

