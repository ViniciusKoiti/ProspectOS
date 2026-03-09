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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.atLeastOnce;
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
}
