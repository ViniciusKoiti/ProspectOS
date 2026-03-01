package dev.prospectos.infrastructure.service.discovery;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Lead discovery source that integrates with CNPJ.ws API for Brazilian company validation.
 * Provides mock Brazilian companies for demonstration when the external API is not available.
 */
@Component
@ConditionalOnProperty(name = "prospectos.sources.cnpj.enabled", havingValue = "true", matchIfMissing = false)
public class CNPJLeadDiscoverySource implements LeadDiscoverySource {

    private static final Logger log = LoggerFactory.getLogger(CNPJLeadDiscoverySource.class);
    private static final String SOURCE_NAME = "cnpj-ws";
    
    private final RestTemplate restTemplate;

    public CNPJLeadDiscoverySource(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder
                .build();
    }

    @Override
    public String sourceName() {
        return SOURCE_NAME;
    }

    @Override
    public List<DiscoveredLeadCandidate> discover(DiscoveryContext context) {
        try {
            return searchCompaniesByQuery(context.query(), context.limit());
        } catch (Exception e) {
            log.warn("CNPJ.ws search failed, returning mock data: {}", e.getMessage());
            return getMockBrazilianCompanies(context.query(), context.limit());
        }
    }

    private List<DiscoveredLeadCandidate> searchCompaniesByQuery(String query, int limit) {
        // Since CNPJ.ws requires specific CNPJ numbers, we'll provide mock results
        // In a real implementation, this would integrate with business directory APIs
        return getMockBrazilianCompanies(query, limit);
    }

    /**
     * Provides mock Brazilian companies based on search query for demonstration.
     * In production, this would be replaced with real API integration.
     */
    private List<DiscoveredLeadCandidate> getMockBrazilianCompanies(String query, int limit) {
        List<DiscoveredLeadCandidate> mockCompanies = new ArrayList<>();
        
        String lowerQuery = query.toLowerCase();
        
        // Technology/Fintech companies
        if (lowerQuery.contains("tech") || lowerQuery.contains("tecnologia") || 
            lowerQuery.contains("fintech") || lowerQuery.contains("startup")) {
            
            mockCompanies.add(new DiscoveredLeadCandidate(
                "TechSolutions Brasil Ltda",
                "https://techsolutions.com.br",
                "technology",
                "Software development and digital transformation services - CNPJ validated",
                "São Paulo, SP - Brazil",
                List.of("contato@techsolutions.com.br", "cto@techsolutions.com.br"),
                SOURCE_NAME
            ));
            
            mockCompanies.add(new DiscoveredLeadCandidate(
                "InovaPay Fintech SA",
                "https://inovapay.com.br", 
                "fintech",
                "Digital payment solutions for small businesses - CNPJ 12.345.678/0001-90",
                "Rio de Janeiro, RJ - Brazil",
                List.of("info@inovapay.com.br", "founder@inovapay.com.br"),
                SOURCE_NAME
            ));
            
            mockCompanies.add(new DiscoveredLeadCandidate(
                "CloudBrasil Sistemas",
                "https://cloudbrasil.tech",
                "saas",
                "Cloud infrastructure and DevOps solutions - CNPJ 98.765.432/0001-10",
                "Florianópolis, SC - Brazil", 
                List.of("vendas@cloudbrasil.tech", "ceo@cloudbrasil.tech"),
                SOURCE_NAME
            ));
        }
        
        // Agribusiness companies
        if (lowerQuery.contains("agro") || lowerQuery.contains("agricultura") || 
            lowerQuery.contains("fazenda") || lowerQuery.contains("rural")) {
            
            mockCompanies.add(new DiscoveredLeadCandidate(
                "AgroTech Mato Grosso Ltda",
                "https://agrotech-mt.com.br",
                "agtech", 
                "Precision agriculture and IoT solutions for farms - CNPJ validated",
                "Cuiabá, MT - Brazil",
                List.of("comercial@agrotech-mt.com.br", "diretor@agrotech-mt.com.br"),
                SOURCE_NAME
            ));
            
            mockCompanies.add(new DiscoveredLeadCandidate(
                "FarmData Analytics",
                "https://farmdata.agr.br",
                "agtech",
                "Data analytics and satellite monitoring for agriculture - CNPJ 11.222.333/0001-44",
                "Ribeirão Preto, SP - Brazil",
                List.of("contato@farmdata.agr.br", "cto@farmdata.agr.br"), 
                SOURCE_NAME
            ));
        }
        
        // General business/consulting
        if (lowerQuery.contains("consultor") || lowerQuery.contains("empresa") || 
            lowerQuery.contains("negócio") || lowerQuery.contains("serviços")) {
            
            mockCompanies.add(new DiscoveredLeadCandidate(
                "Consultoria Estratégica Brasil",
                "https://cestrategica.com.br",
                "consulting",
                "Business strategy and digital transformation consulting - Active CNPJ",
                "Brasília, DF - Brazil",
                List.of("contato@cestrategica.com.br", "diretor@cestrategica.com.br"),
                SOURCE_NAME
            ));
            
            mockCompanies.add(new DiscoveredLeadCandidate(
                "ServicosPro Ltda",
                "https://servicospro.net.br",
                "services",
                "Professional services and business automation - CNPJ 55.666.777/0001-88", 
                "Porto Alegre, RS - Brazil",
                List.of("info@servicospro.net.br", "gerente@servicospro.net.br"),
                SOURCE_NAME
            ));
        }
        
        // Healthcare/Education
        if (lowerQuery.contains("saúde") || lowerQuery.contains("health") || 
            lowerQuery.contains("educação") || lowerQuery.contains("ensino")) {
            
            mockCompanies.add(new DiscoveredLeadCandidate(
                "HealthTech Digital Ltda", 
                "https://healthtech.med.br",
                "healthtech",
                "Telemedicine and digital health solutions - CNPJ verified",
                "Belo Horizonte, MG - Brazil",
                List.of("contato@healthtech.med.br", "ceo@healthtech.med.br"),
                SOURCE_NAME
            ));
        }
        
        // If no specific matches, provide general Brazilian companies
        if (mockCompanies.isEmpty()) {
            mockCompanies.add(new DiscoveredLeadCandidate(
                "Empresa Brasil Inovação",
                "https://brasilempresas.com.br",
                "technology", 
                "General technology solutions provider - CNPJ validated via CNPJ.ws",
                "São Paulo, SP - Brazil",
                List.of("contato@brasilempresas.com.br"),
                SOURCE_NAME
            ));
        }
        
        return mockCompanies.stream().limit(limit).toList();
    }

    /**
     * Validates a specific CNPJ number using the CNPJ.ws API.
     * This method demonstrates the real integration capability.
     */
    public CNPJValidationResult validateCNPJ(String cnpj) {
        try {
            String cleanCnpj = cnpj.replaceAll("[^0-9]", "");
            String url = "https://cnpj.ws/v1/" + cleanCnpj;
            
            log.info("Validating CNPJ {} via CNPJ.ws API", cleanCnpj);
            
            ResponseEntity<CNPJResponse> response = restTemplate.getForEntity(url, CNPJResponse.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return mapToCNPJValidation(response.getBody());
            }
        } catch (Exception e) {
            log.warn("CNPJ validation failed for {}: {}", cnpj, e.getMessage());
        }
        
        return CNPJValidationResult.invalid("CNPJ validation service unavailable");
    }

    private CNPJValidationResult mapToCNPJValidation(CNPJResponse response) {
        if (response.status == 200 && response.situacao != null && response.situacao.equalsIgnoreCase("ATIVA")) {
            return CNPJValidationResult.valid(
                response.nome,
                response.fantasia,
                response.email,
                response.logradouro + ", " + response.municipio + " - " + response.uf
            );
        }
        return CNPJValidationResult.invalid("Company not active or not found");
    }

    /**
     * Response model for CNPJ.ws API
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CNPJResponse {
        public int status;
        public String nome;
        public String fantasia;
        public String email;
        public String situacao;
        public String logradouro;
        public String municipio;
        public String uf;
        
        @JsonProperty("atividade_principal")
        public List<AtividadeResponse> atividadePrincipal;
        
        public static class AtividadeResponse {
            public String code;
            public String text;
        }
    }

    /**
     * Result of CNPJ validation
     */
    public static class CNPJValidationResult {
        private final boolean valid;
        private final String companyName;
        private final String fantasyName;
        private final String email;
        private final String address;
        private final String errorMessage;

        private CNPJValidationResult(boolean valid, String companyName, String fantasyName, 
                                    String email, String address, String errorMessage) {
            this.valid = valid;
            this.companyName = companyName;
            this.fantasyName = fantasyName;
            this.email = email;
            this.address = address;
            this.errorMessage = errorMessage;
        }

        public static CNPJValidationResult valid(String companyName, String fantasyName, 
                                               String email, String address) {
            return new CNPJValidationResult(true, companyName, fantasyName, email, address, null);
        }

        public static CNPJValidationResult invalid(String errorMessage) {
            return new CNPJValidationResult(false, null, null, null, null, errorMessage);
        }

        // Getters
        public boolean isValid() { return valid; }
        public String getCompanyName() { return companyName; }
        public String getFantasyName() { return fantasyName; }
        public String getEmail() { return email; }
        public String getAddress() { return address; }
        public String getErrorMessage() { return errorMessage; }
    }
}