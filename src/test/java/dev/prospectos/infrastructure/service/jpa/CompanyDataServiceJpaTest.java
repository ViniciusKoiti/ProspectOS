package dev.prospectos.infrastructure.service.jpa;

import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.api.dto.request.CompanyCreateRequest;
import dev.prospectos.core.domain.Company;
import dev.prospectos.core.domain.Contact;
import dev.prospectos.core.domain.Email;
import dev.prospectos.core.domain.ICP;
import dev.prospectos.core.domain.Website;
import dev.prospectos.core.repository.CompanyDomainRepository;
import dev.prospectos.core.repository.ICPDomainRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyDataServiceJpaTest {

    @Mock
    private CompanyDomainRepository companyRepository;

    @Mock
    private ICPDomainRepository icpRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private CompanyDataServiceJpa service;

    @BeforeEach
    void setUp() {
        service = new CompanyDataServiceJpa(companyRepository, icpRepository, eventPublisher);
    }

    @Test
    void findCompany_UsesDirectExternalIdLookup() {
        Company company = Company.create("Acme", Website.of("https://acme.com"), "Software");
        company.addContact(new Contact("Alice", Email.of("alice@acme.com"), "CTO", null));
        long externalId = company.getExternalId();

        when(companyRepository.findByExternalId(externalId)).thenReturn(Optional.of(company));

        CompanyDTO found = service.findCompany(externalId);

        assertNotNull(found);
        assertEquals(externalId, found.id());
        assertEquals("Acme", found.name());
        assertEquals("alice@acme.com", found.primaryContactEmail());
        assertEquals(1, found.contactCount());
        verify(companyRepository).findByExternalId(externalId);
        verify(companyRepository, never()).findAll();
    }

    @Test
    void findCompaniesByIcp_UsesDirectIcpExternalIdLookup() {
        ICP icp = ICP.createWithExternalId(
            77L,
            "ICP Tech",
            "Desc",
            List.of("Software"),
            List.of(),
            List.of(),
            "Theme"
        );
        Company company = Company.create("Acme", Website.of("https://acme.com"), "Software");

        when(icpRepository.findByExternalId(77L)).thenReturn(Optional.of(icp));
        when(companyRepository.findByIndustry("Software")).thenReturn(List.of(company));

        List<CompanyDTO> found = service.findCompaniesByICP(77L);

        assertFalse(found.isEmpty());
        verify(icpRepository).findByExternalId(77L);
        verify(icpRepository, never()).findAll();
    }

    @Test
    void findByWebsite_ReturnsNullForInvalidWebsite() {
        CompanyDTO found = service.findByWebsite("://invalid");

        assertNull(found);
        verify(companyRepository, never()).findByWebsiteDomain(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void findCompaniesByIcp_DeduplicatesCompaniesAcrossIndustries() {
        ICP icp = ICP.createWithExternalId(
            88L,
            "ICP Multi",
            "Desc",
            List.of("Software", "Technology"),
            List.of(),
            List.of(),
            "Theme"
        );
        Company shared = Company.create("Shared", Website.of("https://shared.com"), "Software");

        when(icpRepository.findByExternalId(88L)).thenReturn(Optional.of(icp));
        when(companyRepository.findByIndustry("Software")).thenReturn(List.of(shared));
        when(companyRepository.findByIndustry("Technology")).thenReturn(List.of(shared));

        List<CompanyDTO> found = service.findCompaniesByICP(88L);

        assertEquals(1, found.size());
        assertEquals("Shared", found.getFirst().name());
    }

    @Test
    void createCompany_ThrowsForInvalidSize() {
        CompanyCreateRequest request = new CompanyCreateRequest(
            "Acme",
            "Software",
            "https://acme.com",
            "Desc",
            null,
            null,
            "tiny"
        );

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.createCompany(request)
        );

        assertEquals("Invalid company size: tiny", exception.getMessage());
        verify(companyRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void addCompanyContactEmails_PersistsOnlyValidUniqueEmails() {
        Company company = Company.create("Acme", Website.of("https://acme.com"), "Software");
        long externalId = company.getExternalId();

        when(companyRepository.findByExternalId(externalId)).thenReturn(Optional.of(company));

        service.addCompanyContactEmails(externalId, List.of(
            "first@acme.com",
            "invalid-email",
            "FIRST@acme.com",
            "second@acme.com",
            ""
        ));

        assertEquals(2, company.getContacts().size());
        assertEquals("first@acme.com", company.getContacts().getFirst().getEmail().getAddress());
        assertEquals("second@acme.com", company.getContacts().get(1).getEmail().getAddress());
        verify(companyRepository).save(company);
    }
}
