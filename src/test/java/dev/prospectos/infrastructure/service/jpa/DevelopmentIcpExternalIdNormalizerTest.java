package dev.prospectos.infrastructure.service.jpa;

import dev.prospectos.core.domain.ExternalIdPolicy;
import dev.prospectos.core.domain.ICP;
import dev.prospectos.core.repository.ICPDomainRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DevelopmentIcpExternalIdNormalizerTest {

    @Mock
    private ICPDomainRepository icpRepository;

    @Mock
    private ApplicationArguments args;

    private DevelopmentIcpExternalIdNormalizer normalizer;

    @BeforeEach
    void setUp() {
        normalizer = new DevelopmentIcpExternalIdNormalizer(icpRepository);
    }

    @Test
    void runNormalizesUnsafeIcpsUsingNextAvailableSafeIds() throws Exception {
        ICP safe = org.mockito.Mockito.mock(ICP.class);
        ICP unsafeA = org.mockito.Mockito.mock(ICP.class);
        ICP unsafeB = org.mockito.Mockito.mock(ICP.class);

        when(safe.getExternalId()).thenReturn(5L);
        when(unsafeA.getExternalId()).thenReturn(ExternalIdPolicy.MAX_SAFE_JS_INTEGER + 1);
        when(unsafeB.getExternalId()).thenReturn(ExternalIdPolicy.MAX_SAFE_JS_INTEGER + 2);
        when(icpRepository.findAll()).thenReturn(List.of(safe, unsafeA, unsafeB));

        normalizer.run(args);

        verify(unsafeA).normalizeExternalId(6L);
        verify(unsafeB).normalizeExternalId(7L);
        verify(icpRepository).save(unsafeA);
        verify(icpRepository).save(unsafeB);
        verify(safe, never()).normalizeExternalId(anyLong());
    }

    @Test
    void runSkipsSaveWhenAllIcpIdsAreAlreadySafe() throws Exception {
        ICP icpA = org.mockito.Mockito.mock(ICP.class);
        ICP icpB = org.mockito.Mockito.mock(ICP.class);
        when(icpA.getExternalId()).thenReturn(1L);
        when(icpB.getExternalId()).thenReturn(2L);
        when(icpRepository.findAll()).thenReturn(List.of(icpA, icpB));

        normalizer.run(args);

        verify(icpRepository, never()).save(any());
    }

    @Test
    void runHandlesNullUnsafeExternalId() throws Exception {
        ICP unsafeNull = org.mockito.Mockito.mock(ICP.class);
        when(unsafeNull.getExternalId()).thenReturn(null);
        when(icpRepository.findAll()).thenReturn(List.of(unsafeNull));

        normalizer.run(args);

        verify(unsafeNull).normalizeExternalId(1L);
        verify(icpRepository).save(unsafeNull);
    }
}