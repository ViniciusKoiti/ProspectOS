package dev.prospectos.infrastructure.service.inmemory;

import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.api.dto.ScoreDTO;
import dev.prospectos.api.dto.request.CompanyCreateRequest;
import dev.prospectos.api.dto.request.CompanyUpdateRequest;
import dev.prospectos.infrastructure.service.discovery.CompanyVectorReindexRequested;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InMemoryCompanyDataServiceTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private InMemoryCompanyDataService service;

    @BeforeEach
    void setUp() {
        service = new InMemoryCompanyDataService(new InMemoryCoreDataStore(), eventPublisher);
    }

    @Test
    void findByWebsiteMatchesByNormalizedDomain() {
        CompanyDTO found = service.findByWebsite("http://www.techcorp.com");

        assertNotNull(found);
        assertEquals("TechCorp", found.name());
    }

    @Test
    void findByWebsiteReturnsNullForInvalidWebsite() {
        assertNull(service.findByWebsite("not a valid url with spaces"));
    }

    @Test
    void updateCompanyPreservesExistingScore() {
        CompanyDTO created = service.createCompany(new CompanyCreateRequest(
            "Acme",
            "Software",
            "https://acme.example",
            "desc",
            "BR",
            "Sao Paulo",
            "SMALL"
        ));
        service.updateCompanyScore(created.id(), new ScoreDTO(88, "HOT", "Strong fit"));

        CompanyDTO updated = service.updateCompany(created.id(), new CompanyUpdateRequest(
            "Acme Updated",
            "Software",
            "https://acme.example",
            "desc2",
            "BR",
            "Rio",
            "MEDIUM"
        ));

        assertNotNull(updated);
        assertNotNull(updated.score());
        assertEquals(88, updated.score().value());
        assertEquals("HOT", updated.score().category());
    }

    @Test
    void createAndScoreUpdatePublishReindexEvents() {
        CompanyDTO created = service.createCompany(new CompanyCreateRequest(
            "Event Co",
            "Services",
            "https://events.example",
            "desc",
            "BR",
            "Curitiba",
            "SMALL"
        ));
        service.updateCompanyScore(created.id(), new ScoreDTO(55, "WARM", "ok"));

        ArgumentCaptor<CompanyVectorReindexRequested> captor = ArgumentCaptor.forClass(CompanyVectorReindexRequested.class);
        verify(eventPublisher, atLeastOnce()).publishEvent(captor.capture());
        assertEquals(created.id(), captor.getValue().companyId());
    }

    @Test
    void findCompanyReturnsStoredCompany() {
        CompanyDTO first = service.findAllCompanies().getFirst();

        CompanyDTO found = service.findCompany(first.id());

        assertEquals(first, found);
    }

    @Test
    void findAllCompaniesReturnsCopy() {
        List<CompanyDTO> all = service.findAllCompanies();

        assertThrows(UnsupportedOperationException.class, () -> all.add(all.getFirst()));
    }

    @Test
    void updateCompanyReturnsNullWhenCompanyDoesNotExist() {
        CompanyDTO updated = service.updateCompany(9999L, new CompanyUpdateRequest(
            "Missing",
            "Tech",
            "https://missing.example",
            null,
            null,
            null,
            "SMALL"
        ));

        assertNull(updated);
    }

    @Test
    void deleteCompanyRemovesCompanyAndContactsAndPublishesEvent() {
        CompanyDTO created = service.createCompany(new CompanyCreateRequest(
            "Delete Me",
            "Software",
            "https://delete.example",
            "desc",
            "BR",
            "Florianopolis",
            "SMALL"
        ));
        service.addCompanyContactEmails(created.id(), List.of("owner@delete.example"));

        boolean deleted = service.deleteCompany(created.id());

        assertTrue(deleted);
        assertNull(service.findCompany(created.id()));
        assertTrue(service.findCompanyContacts(created.id()).isEmpty());
        verify(eventPublisher, atLeastOnce()).publishEvent(any(CompanyVectorReindexRequested.class));
    }

    @Test
    void deleteCompanyReturnsFalseWhenCompanyDoesNotExist() {
        boolean deleted = service.deleteCompany(424242L);

        assertFalse(deleted);
        verify(eventPublisher, never()).publishEvent(any(CompanyVectorReindexRequested.class));
    }

    @Test
    void updateCompanyScoreThrowsWhenCompanyDoesNotExist() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.updateCompanyScore(777L, new ScoreDTO(10, "COLD", "reason"))
        );

        assertTrue(exception.getMessage().contains("Company not found"));
    }

    @Test
    void addCompanyContactEmailsThrowsWhenCompanyDoesNotExist() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.addCompanyContactEmails(777L, List.of("test@example.com"))
        );

        assertTrue(exception.getMessage().contains("Company not found"));
    }

    @Test
    void addCompanyContactEmailsIgnoresInvalidOrDuplicateEmails() {
        CompanyDTO created = service.createCompany(new CompanyCreateRequest(
            "Contacts Co",
            "Software",
            "https://contacts.example",
            "desc",
            "BR",
            "Sao Paulo",
            "SMALL"
        ));

        service.addCompanyContactEmails(created.id(), List.of("contact@contacts.example"));
        service.addCompanyContactEmails(created.id(), List.of("invalid", "CONTACT@contacts.example", ""));

        CompanyDTO updated = service.findCompany(created.id());
        assertNotNull(updated);
        assertEquals(1, updated.contactCount());
        assertEquals("contact@contacts.example", updated.primaryContactEmail());
        verify(eventPublisher, times(2)).publishEvent(any(CompanyVectorReindexRequested.class));
    }

    @Test
    void findCompaniesByIcpReturnsEmptyForUnknownIcp() {
        List<CompanyDTO> companies = service.findCompaniesByICP(999L);

        assertTrue(companies.isEmpty());
    }
}