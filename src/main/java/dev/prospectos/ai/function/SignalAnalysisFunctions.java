package dev.prospectos.ai.function;

import dev.prospectos.core.domain.Company;
import dev.prospectos.core.repository.CompanyDomainRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Configuration
public class SignalAnalysisFunctions {
    
    private final CompanyDomainRepository companyRepository;
    
    public SignalAnalysisFunctions(CompanyDomainRepository companyRepository) {
        this.companyRepository = companyRepository;
    }
    
    @Bean
    @Description("""
        Analyzes interest signals for a company.
        Returns a summary of detected signals grouped by type.
        """)
    public Function<SignalRequest, Map<String, Object>> analyzeCompanySignals() {
        return request -> {
            log.info("LLM called analyzeCompanySignals: {}", request.companyId());
            
            Company company = companyRepository.findById(request.companyId())
                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + request.companyId()));
            
            return Map.of(
                "totalSignals", company.getTechnologySignals().size(),
                "signalsByType", Map.of(
                    "TECHNOLOGY_ADOPTION", company.getTechnologySignals().size()
                ),
                "hasActiveSignals", company.hasActiveSignals(),
                "mostRecentSignal", company.getTechnologySignals().stream()
                    .map(s -> s.getTechnology() + ": " + s.getDescription())
                    .findFirst()
                    .orElse("No signals")
            );
        };
    }
    
    public record SignalRequest(
        @Description("Company ID to analyze")
        UUID companyId
    ) {}
}
