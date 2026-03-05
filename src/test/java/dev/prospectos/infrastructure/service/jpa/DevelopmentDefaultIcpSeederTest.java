package dev.prospectos.infrastructure.service.jpa;

import dev.prospectos.core.domain.ICP;
import dev.prospectos.core.repository.ICPDomainRepository;
import dev.prospectos.infrastructure.config.LeadSearchProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DevelopmentDefaultIcpSeederTest {

    @Test
    void runSkipsWhenDefaultIcpIdIsMissing() throws Exception {
        ICPDomainRepository repository = mock(ICPDomainRepository.class);
        DevelopmentDefaultIcpSeeder seeder = new DevelopmentDefaultIcpSeeder(repository, new LeadSearchProperties(null));

        seeder.run(new DefaultApplicationArguments());

        verify(repository, never()).findByExternalId(any());
        verify(repository, never()).save(any());
    }

    @Test
    void runSkipsWhenDefaultIcpAlreadyExists() throws Exception {
        ICPDomainRepository repository = mock(ICPDomainRepository.class);
        when(repository.findByExternalId(7L)).thenReturn(Optional.of(mock(ICP.class)));
        DevelopmentDefaultIcpSeeder seeder = new DevelopmentDefaultIcpSeeder(repository, new LeadSearchProperties(7L));

        seeder.run(new DefaultApplicationArguments());

        verify(repository).findByExternalId(7L);
        verify(repository, never()).save(any());
    }

    @Test
    void runCreatesDefaultIcpWhenMissing() throws Exception {
        ICPDomainRepository repository = mock(ICPDomainRepository.class);
        when(repository.findByExternalId(7L)).thenReturn(Optional.empty());
        DevelopmentDefaultIcpSeeder seeder = new DevelopmentDefaultIcpSeeder(repository, new LeadSearchProperties(7L));

        seeder.run(new DefaultApplicationArguments());

        verify(repository).findByExternalId(7L);
        verify(repository).save(any(ICP.class));
    }
}
