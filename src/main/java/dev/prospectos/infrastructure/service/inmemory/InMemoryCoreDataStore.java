package dev.prospectos.infrastructure.service.inmemory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.api.dto.ICPDto;
import dev.prospectos.api.dto.ScoreDTO;

@Component
@Profile("!test-pg & (test | development)")
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

    public Map<Long, ScoreDTO> companyScores() {
        return companyScores;
    }

    long nextCompanyId() {
        return companyIdSequence.incrementAndGet();
    }

    long nextIcpId() {
        return icpIdSequence.incrementAndGet();
    }

    private void seedData() {
        companies.putAll(InMemorySeedData.companies());
        ICPDto icp = InMemorySeedData.icp();
        icps.put(icp.id(), icp);
        icpCompanies.put(icp.id(), new ArrayList<>(InMemorySeedData.icpCompanyIds()));
    }

    private void initializeSequences() {
        long maxCompanyId = companies.keySet().stream().mapToLong(Long::longValue).max().orElse(0L);
        long maxIcpId = icps.keySet().stream().mapToLong(Long::longValue).max().orElse(0L);
        companyIdSequence.set(maxCompanyId);
        icpIdSequence.set(maxIcpId);
    }
}
