package dev.prospectos.infrastructure.service.jpa;

import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.core.domain.Company;
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
        long externalId = company.getExternalId();

        when(companyRepository.findByExternalId(externalId)).thenReturn(Optional.of(company));

        CompanyDTO found = service.findCompany(externalId);

        assertNotNull(found);
        assertEquals(externalId, found.id());
        assertEquals("Acme", found.name());
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
}
