package dev.prospectos.infrastructure.adapter;

import dev.prospectos.core.domain.Company;
import dev.prospectos.core.domain.CompanySize;
import dev.prospectos.core.domain.ProspectingStatus;
import dev.prospectos.infrastructure.jpa.CompanyJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyRepositoryAdapterTest {

    @Mock
    private CompanyJpaRepository jpaRepository;

    @Mock
    private Company company;

    private CompanyRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new CompanyRepositoryAdapter(jpaRepository);
    }

    @Test
    void delegatesCrudAndBasicQueries() {
        UUID id = UUID.randomUUID();
        Instant since = Instant.now();
        when(jpaRepository.save(company)).thenReturn(company);
        when(jpaRepository.findById(id)).thenReturn(Optional.of(company));
        when(jpaRepository.findByExternalId(10L)).thenReturn(Optional.of(company));
        when(jpaRepository.findAll()).thenReturn(List.of(company));
        when(jpaRepository.findByWebsiteUrl("https://acme.com")).thenReturn(Optional.of(company));
        when(jpaRepository.findByIndustry("Technology")).thenReturn(List.of(company));
        when(jpaRepository.findByStatus(ProspectingStatus.NEW)).thenReturn(List.of(company));
        when(jpaRepository.findByMinScore(70.0)).thenReturn(List.of(company));
        when(jpaRepository.findByScoreAndStatus(70.0, ProspectingStatus.REVIEWING)).thenReturn(List.of(company));
        when(jpaRepository.findTopQualifiedProspects()).thenReturn(List.of(company));
        when(jpaRepository.findByIndustryAndSize("Technology", CompanySize.MEDIUM)).thenReturn(List.of(company));
        when(jpaRepository.findByCountryAndMinScore("BR", 50.0)).thenReturn(List.of(company));
        when(jpaRepository.findRecentlyUpdatedProspects(since)).thenReturn(List.of(company));
        when(jpaRepository.findRecentlyAdded(since)).thenReturn(List.of(company));
        when(jpaRepository.countByStatus(ProspectingStatus.QUALIFIED)).thenReturn(4L);
        when(jpaRepository.getAverageScoreByIndustry("Technology")).thenReturn(81.5);
        when(jpaRepository.findCompaniesWithoutContacts()).thenReturn(List.of(company));
        when(jpaRepository.findCompaniesWithTechnologySignals()).thenReturn(List.of(company));
        when(jpaRepository.findByWebsiteDomain("acme.com")).thenReturn(List.of(company));
        when(jpaRepository.findStaleProspects(40.0, since)).thenReturn(List.of(company));

        assertThat(adapter.save(company)).isSameAs(company);
        assertThat(adapter.findById(id)).contains(company);
        assertThat(adapter.findByExternalId(10L)).contains(company);
        adapter.delete(company);
        assertThat(adapter.findAll()).containsExactly(company);
        assertThat(adapter.findByWebsiteUrl("https://acme.com")).contains(company);
        assertThat(adapter.findByIndustry("Technology")).containsExactly(company);
        assertThat(adapter.findByStatus(ProspectingStatus.NEW)).containsExactly(company);
        assertThat(adapter.findByMinScore(70.0)).containsExactly(company);
        assertThat(adapter.findByScoreAndStatus(70.0, ProspectingStatus.REVIEWING)).containsExactly(company);
        assertThat(adapter.findTopQualifiedProspects()).containsExactly(company);
        assertThat(adapter.findByIndustryAndSize("Technology", CompanySize.MEDIUM)).containsExactly(company);
        assertThat(adapter.findByCountryAndMinScore("BR", 50.0)).containsExactly(company);
        assertThat(adapter.findRecentlyUpdated(since)).containsExactly(company);
        assertThat(adapter.findRecentlyAdded(since)).containsExactly(company);
        assertThat(adapter.countByStatus(ProspectingStatus.QUALIFIED)).isEqualTo(4L);
        assertThat(adapter.getAverageScoreByIndustry("Technology")).isEqualTo(81.5);
        assertThat(adapter.findCompaniesWithoutContacts()).containsExactly(company);
        assertThat(adapter.findCompaniesWithTechnologySignals()).containsExactly(company);
        assertThat(adapter.findByWebsiteDomain("acme.com")).containsExactly(company);
        assertThat(adapter.findStaleProspects(40.0, since)).containsExactly(company);

        verify(jpaRepository).delete(company);
    }

    @Test
    void findRecentlyUpdatedDelegatesToRepositorySpecificMethod() {
        Instant since = Instant.now();
        when(jpaRepository.findRecentlyUpdatedProspects(since)).thenReturn(List.of(company));

        List<Company> result = adapter.findRecentlyUpdated(since);

        assertThat(result).containsExactly(company);
        verify(jpaRepository).findRecentlyUpdatedProspects(eq(since));
    }
}

