package dev.prospectos.infrastructure.service.inmemory;

import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.api.dto.ICPDto;
import dev.prospectos.api.dto.ScoreDTO;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Profile({"demo", "test", "mock"})
public class InMemoryCoreDataStore {

    private final Map<Long, CompanyDTO> companies = new ConcurrentHashMap<>();
    private final Map<Long, ICPDto> icps = new ConcurrentHashMap<>();
    private final Map<Long, List<Long>> icpCompanies = new ConcurrentHashMap<>();
    private final Map<Long, ScoreDTO> companyScores = new ConcurrentHashMap<>();
    private final AtomicLong companyIdSequence = new AtomicLong();
    private final AtomicLong icpIdSequence = new AtomicLong();

    public InMemoryCoreDataStore() {
        seedData();
        initializeSequences();
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

    long nextCompanyId() {
        return companyIdSequence.incrementAndGet();
    }

    long nextIcpId() {
        return icpIdSequence.incrementAndGet();
    }

    private void seedData() {
        CompanyDTO techCorp = new CompanyDTO(
            1L,
            "TechCorp",
            "Software",
            "https://techcorp.com",
            "Leading software company",
            150,
            "San Francisco, CA"
        );
        CompanyDTO cloudTech = new CompanyDTO(
            2L,
            "CloudTech Solutions",
            "Software",
            "https://cloudtech.com",
            "Cloud infrastructure specialists",
            220,
            "Austin, TX"
        );
        CompanyDTO localRestaurant = new CompanyDTO(
            3L,
            "Local Restaurant",
            "Food & Beverage",
            "https://localrestaurant.com",
            "Neighborhood dining spot",
            25,
            "Curitiba, BR"
        );
        CompanyDTO techStart1 = new CompanyDTO(
            4L,
            "TechStart1",
            "Software",
            "https://techstart1.com",
            "Early-stage software startup",
            40,
            "Sao Paulo, BR"
        );
        CompanyDTO techStart2 = new CompanyDTO(
            5L,
            "TechStart2",
            "Software",
            "https://techstart2.com",
            "Growing SaaS platform",
            55,
            "Toronto, CA"
        );
        CompanyDTO techStart3 = new CompanyDTO(
            6L,
            "TechStart3",
            "Software",
            "https://techstart3.com",
            "Product-led startup",
            65,
            "Miami, FL"
        );
        CompanyDTO minimalCorp = new CompanyDTO(
            7L,
            "MinimalCorp",
            "Unknown",
            "https://minimal.com",
            "Minimal company profile",
            10,
            "Remote"
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

        companies.put(techCorp.id(), techCorp);
        companies.put(cloudTech.id(), cloudTech);
        companies.put(localRestaurant.id(), localRestaurant);
        companies.put(techStart1.id(), techStart1);
        companies.put(techStart2.id(), techStart2);
        companies.put(techStart3.id(), techStart3);
        companies.put(minimalCorp.id(), minimalCorp);
        icps.put(icp.id(), icp);
        icpCompanies.put(icp.id(), new ArrayList<>(List.of(
            techCorp.id(),
            cloudTech.id(),
            localRestaurant.id(),
            techStart1.id(),
            techStart2.id(),
            techStart3.id(),
            minimalCorp.id()
        )));
    }

    private void initializeSequences() {
        long maxCompanyId = companies.keySet().stream().mapToLong(Long::longValue).max().orElse(0L);
        long maxIcpId = icps.keySet().stream().mapToLong(Long::longValue).max().orElse(0L);
        companyIdSequence.set(maxCompanyId);
        icpIdSequence.set(maxIcpId);
    }
}
