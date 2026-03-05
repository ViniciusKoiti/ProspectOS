package dev.prospectos.infrastructure.adapter;

import dev.prospectos.core.domain.ICP;
import dev.prospectos.infrastructure.jpa.ICPJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ICPRepositoryAdapterTest {

    @Mock
    private ICPJpaRepository jpaRepository;

    @Mock
    private ICP icp;

    private ICPRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ICPRepositoryAdapter(jpaRepository);
    }

    @Test
    void delegatesAllRepositoryOperations() {
        UUID id = UUID.randomUUID();
        when(jpaRepository.save(icp)).thenReturn(icp);
        when(jpaRepository.findById(id)).thenReturn(Optional.of(icp));
        when(jpaRepository.findByExternalId(5L)).thenReturn(Optional.of(icp));
        when(jpaRepository.findAll()).thenReturn(List.of(icp));
        when(jpaRepository.findByIndustry("Technology")).thenReturn(List.of(icp));
        when(jpaRepository.findByTargetRole("CTO")).thenReturn(List.of(icp));
        when(jpaRepository.findByName("Default")).thenReturn(Optional.of(icp));
        when(jpaRepository.findActiveICPs()).thenReturn(List.of(icp));
        when(jpaRepository.findDefaultICP()).thenReturn(icp);

        assertThat(adapter.save(icp)).isSameAs(icp);
        assertThat(adapter.findById(id)).contains(icp);
        assertThat(adapter.findByExternalId(5L)).contains(icp);
        adapter.delete(icp);
        assertThat(adapter.findAll()).containsExactly(icp);
        assertThat(adapter.findByIndustry("Technology")).containsExactly(icp);
        assertThat(adapter.findByTargetRole("CTO")).containsExactly(icp);
        assertThat(adapter.findByName("Default")).contains(icp);
        assertThat(adapter.findActiveICPs()).containsExactly(icp);
        assertThat(adapter.findDefaultICP()).isSameAs(icp);

        verify(jpaRepository).delete(icp);
    }
}
