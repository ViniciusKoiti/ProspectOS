package dev.prospectos.infrastructure.service.jpa;

import dev.prospectos.core.domain.Company;
import dev.prospectos.core.domain.ExternalIdPolicy;
import dev.prospectos.core.repository.CompanyDomainRepository;
import dev.prospectos.infrastructure.api.companies.SourceProvenance;
import dev.prospectos.infrastructure.jpa.SourceProvenanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DevelopmentCompanyExternalIdNormalizerTest {

    @Mock
    private CompanyDomainRepository companyRepository;

    @Mock
    private SourceProvenanceRepository sourceProvenanceRepository;

    @Mock
    private ApplicationArguments args;

    private DevelopmentCompanyExternalIdNormalizer normalizer;

    @BeforeEach
    void setUp() {
        normalizer = new DevelopmentCompanyExternalIdNormalizer(companyRepository, sourceProvenanceRepository);
    }

    @Test
    void runNormalizesUnsafeCompaniesAndRewiresProvenance() throws Exception {
        Company safe = org.mockito.Mockito.mock(Company.class);
        Company unsafe = org.mockito.Mockito.mock(Company.class);
        Long unsafeId = ExternalIdPolicy.MAX_SAFE_JS_INTEGER + 100;

        when(safe.getExternalId()).thenReturn(10L);
        when(unsafe.getExternalId()).thenReturn(unsafeId);
        when(companyRepository.findAll()).thenReturn(List.of(safe, unsafe));

        SourceProvenance provenance = SourceProvenance.of(unsafeId, "scraper", "https://example.com", Instant.now());
        when(sourceProvenanceRepository.findByCompanyExternalId(unsafeId)).thenReturn(List.of(provenance));

        normalizer.run(args);

        verify(safe, never()).normalizeExternalId(anyLong());
        verify(unsafe).normalizeExternalId(11L);
        verify(companyRepository).save(unsafe);
        verify(sourceProvenanceRepository).findByCompanyExternalId(unsafeId);

        ArgumentCaptor<List<SourceProvenance>> captor = ArgumentCaptor.forClass(List.class);
        verify(sourceProvenanceRepository).saveAll(captor.capture());
        assertEquals(11L, captor.getValue().getFirst().getCompanyExternalId());
    }

    @Test
    void runSkipsProvenanceLookupWhenOldIdIsNull() throws Exception {
        Company unsafeWithNullId = org.mockito.Mockito.mock(Company.class);
        when(unsafeWithNullId.getExternalId()).thenReturn(null);
        when(companyRepository.findAll()).thenReturn(List.of(unsafeWithNullId));

        normalizer.run(args);

        verify(unsafeWithNullId).normalizeExternalId(1L);
        verify(companyRepository).save(unsafeWithNullId);
        verify(sourceProvenanceRepository, never()).findByCompanyExternalId(any());
        verify(sourceProvenanceRepository, never()).saveAll(any());
    }

    @Test
    void runSkipsSavingWhenAllIdsAreAlreadySafe() throws Exception {
        Company companyA = org.mockito.Mockito.mock(Company.class);
        Company companyB = org.mockito.Mockito.mock(Company.class);
        when(companyA.getExternalId()).thenReturn(1L);
        when(companyB.getExternalId()).thenReturn(2L);
        when(companyRepository.findAll()).thenReturn(List.of(companyA, companyB));

        normalizer.run(args);

        verify(companyRepository, never()).save(any());
        verifyNoInteractions(sourceProvenanceRepository);
    }

    @Test
    void runDoesNotPersistProvenanceWhenNoRecordsAreFound() throws Exception {
        Company unsafe = org.mockito.Mockito.mock(Company.class);
        Long unsafeId = ExternalIdPolicy.MAX_SAFE_JS_INTEGER + 20;

        when(unsafe.getExternalId()).thenReturn(unsafeId);
        when(companyRepository.findAll()).thenReturn(List.of(unsafe));
        when(sourceProvenanceRepository.findByCompanyExternalId(unsafeId)).thenReturn(List.of());

        normalizer.run(args);

        verify(unsafe).normalizeExternalId(1L);
        verify(companyRepository).save(unsafe);
        verify(sourceProvenanceRepository).findByCompanyExternalId(unsafeId);
        verify(sourceProvenanceRepository, never()).saveAll(any());
    }
}