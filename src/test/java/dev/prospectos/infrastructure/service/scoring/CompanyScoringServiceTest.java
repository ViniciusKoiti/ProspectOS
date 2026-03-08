package dev.prospectos.infrastructure.service.scoring;

import dev.prospectos.ai.dto.PriorityLevel;
import dev.prospectos.ai.dto.ScoringResult;
import dev.prospectos.ai.service.ScoringAIService;
import dev.prospectos.api.CompanyDataService;
import dev.prospectos.api.ICPDataService;
import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.api.dto.ICPDto;
import dev.prospectos.api.dto.ScoreDTO;
import dev.prospectos.core.domain.Company;
import dev.prospectos.core.domain.ICP;
import dev.prospectos.core.domain.Website;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyScoringServiceTest {

    @Mock
    private ScoringAIService scoringAIService;

    @Mock
    private CompanyDataService companyDataService;

    @Mock
    private ICPDataService icpDataService;

    private CompanyScoringService companyScoringService;

    @BeforeEach
    void setUp() {
        companyScoringService = new CompanyScoringService(
                scoringAIService,
                companyDataService,
                icpDataService
        );
    }

    @Test
    void scoreCompany_MapsScoreAndUpdatesCompany() {
        // Arrange
        CompanyDTO company = new CompanyDTO(
                1L,
                "Acme",
                "Software",
                "https://acme.com",
                "Test company",
                120,
                "Austin",
                null
        );
        ICPDto icp = new ICPDto(
                1L,
                "ICP",
                "Desc",
                List.of("Software"),
                List.of("LATAM"),
                List.of("Java"),
                10,
                500,
                List.of("CTO"),
                "Growth signals"
        );

        when(companyDataService.findCompany(1L)).thenReturn(company);
        when(icpDataService.findICP(1L)).thenReturn(icp);
        when(scoringAIService.scoreCompany(any(), any())).thenReturn(new ScoringResult(
                88,
                PriorityLevel.HOT,
                "Strong fit",
                Map.of("icpFit", 28),
                "Engage now"
        ));

        // Act
        ScoreDTO result = companyScoringService.scoreCompany(1L, 1L);

        // Assert
        assertEquals(88, result.value());
        assertEquals("HOT", result.category());
        assertEquals("Strong fit", result.reasoning());

        ArgumentCaptor<ScoreDTO> scoreCaptor = ArgumentCaptor.forClass(ScoreDTO.class);
        verify(companyDataService).updateCompanyScore(eq(1L), scoreCaptor.capture());
        assertEquals(88, scoreCaptor.getValue().value());
        assertEquals("HOT", scoreCaptor.getValue().category());
    }

    @Test
    void scoreCompany_ClampsOutOfRangeScore() {
        // Arrange
        CompanyDTO company = CompanyDTO.createMock();
        ICPDto icp = ICPDto.createMock();

        when(companyDataService.findCompany(1L)).thenReturn(company);
        when(icpDataService.findICP(1L)).thenReturn(icp);
        when(scoringAIService.scoreCompany(any(), any())).thenReturn(new ScoringResult(
                150,
                PriorityLevel.WARM,
                "Over limit",
                Map.of(),
                "Follow up"
        ));

        // Act
        ScoreDTO result = companyScoringService.scoreCompany(1L, 1L);

        // Assert
        assertEquals(100, result.value());
        assertEquals("WARM", result.category());
    }

    @Test
    void scoreCompany_UsesFallbackOnFailure() {
        // Arrange
        CompanyDTO company = CompanyDTO.createMock();
        ICPDto icp = ICPDto.createMock();

        when(companyDataService.findCompany(1L)).thenReturn(company);
        when(icpDataService.findICP(1L)).thenReturn(icp);
        when(scoringAIService.scoreCompany(any(), any()))
                .thenThrow(new RuntimeException("timeout"));

        // Act
        ScoreDTO result = companyScoringService.scoreCompany(1L, 1L);

        // Assert
        assertEquals(0, result.value());
        assertEquals("IGNORE", result.category());
        assertTrue(result.reasoning().contains("timeout"));
    }

    @Test
    void scoreCompany_RejectsMissingCompany() {
        when(companyDataService.findCompany(1L)).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> companyScoringService.scoreCompany(1L, 1L));
    }

    @Test
    void scoreCompany_RejectsMissingIcp() {
        when(companyDataService.findCompany(1L)).thenReturn(CompanyDTO.createMock());
        when(icpDataService.findICP(1L)).thenReturn(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> companyScoringService.scoreCompany(1L, 1L));
    }

    @Test
    void scoreCandidate_UsesDefaultPriorityWhenAiReturnsNullPriority() {
        Company company = Company.create("Acme", Website.of("https://acme.com"), "Software");
        ICP icp = ICP.create("ICP", "Desc", List.of("Software"), List.of("BR"), List.of("CTO"), "Growth");
        when(scoringAIService.scoreCompany(any(), any())).thenReturn(new ScoringResult(
            55,
            null,
            "No explicit priority",
            Map.of(),
            "Observe"
        ));

        ScoreDTO result = companyScoringService.scoreCandidate(company, icp);

        assertEquals(55, result.value());
        assertEquals("COLD", result.category());
    }
}
