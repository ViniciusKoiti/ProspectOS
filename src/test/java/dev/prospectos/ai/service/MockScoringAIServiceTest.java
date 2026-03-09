package dev.prospectos.ai.service;

import dev.prospectos.ai.dto.PriorityLevel;
import dev.prospectos.ai.dto.ScoringResult;
import dev.prospectos.core.domain.Company;
import dev.prospectos.core.domain.ICP;
import dev.prospectos.core.domain.Website;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MockScoringAIServiceTest {

    @Test
    void scoreCompanyReturnsValidRangePriorityAndBreakdown() {
        MockScoringAIService service = new MockScoringAIService();
        Company company = Company.create("Acme Tech", Website.of("https://acme.com"), "Fintech");
        company.setLocation("São Paulo", "Brazil");
        ICP icp = ICP.create("ICP", "Desc", List.of("Fintech"), List.of("BR"), List.of("CTO"), "Growth");

        ScoringResult result = service.scoreCompany(company, icp);

        assertTrue(result.score() >= 45 && result.score() <= 95);
        assertTrue(result.breakdown().containsKey("icpFit"));
        assertTrue(result.reasoning().contains("PRIORITY"));
        assertEquals(expectedPriority(result.score()), result.priority());
    }

    @Test
    void firstScoreIsDeterministicAcrossNewInstancesWithFixedSeed() {
        Company company = Company.create("Deterministic Startup", Website.of("https://det.example"), "Technology");
        ICP icp = ICP.create("ICP", "Desc", List.of("Tech"), List.of("BR"), List.of("CTO"), "Growth");

        ScoringResult first = new MockScoringAIService().scoreCompany(company, icp);
        ScoringResult second = new MockScoringAIService().scoreCompany(company, icp);

        assertEquals(first.score(), second.score());
        assertEquals(first.priority(), second.priority());
        assertEquals(first.recommendation(), second.recommendation());
    }

    private PriorityLevel expectedPriority(int score) {
        if (score >= 80) {
            return PriorityLevel.HOT;
        }
        if (score >= 65) {
            return PriorityLevel.WARM;
        }
        return PriorityLevel.COLD;
    }
}
