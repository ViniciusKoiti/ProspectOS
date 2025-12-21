package dev.prospectos.infrastructure.service.inmemory;

import dev.prospectos.core.api.dto.CompanyDTO;
import dev.prospectos.core.api.dto.ICPDto;
import dev.prospectos.core.api.dto.ScoreDTO;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Profile({"demo", "test"})
public class InMemoryCoreDataStore {

    private final Map<Long, CompanyDTO> companies = new ConcurrentHashMap<>();
    private final Map<Long, ICPDto> icps = new ConcurrentHashMap<>();
    private final Map<Long, List<Long>> icpCompanies = new ConcurrentHashMap<>();
    private final Map<Long, ScoreDTO> companyScores = new ConcurrentHashMap<>();

    public InMemoryCoreDataStore() {
        seedData();
    }

    Map<Long, CompanyDTO> companies() {
        return companies;
    }

    Map<Long, ICPDto> icps() {
        return icps;
    }

    Map<Long, List<Long>> icpCompanies() {
        return icpCompanies;
    }

    Map<Long, ScoreDTO> companyScores() {
        return companyScores;
    }

    private void seedData() {
        CompanyDTO company = new CompanyDTO(
            1L,
            "TechCorp",
            "Software",
            "https://techcorp.com",
            "Leading software company",
            150,
            "San Francisco, CA"
        );
        ICPDto icp = new ICPDto(
            1L,
            "DevOps Teams",
            "Target companies with active DevOps practices",
            List.of("Software", "Technology", "SaaS"),
            List.of("Docker", "Kubernetes", "AWS", "Jenkins"),
            50,
            500,
            List.of("CTO", "DevOps Engineer", "Platform Engineer")
        );

        companies.put(company.id(), company);
        icps.put(icp.id(), icp);
        icpCompanies.put(icp.id(), new ArrayList<>(List.of(company.id())));
    }
}
