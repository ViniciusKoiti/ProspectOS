package dev.prospectos.infrastructure.service.discovery;

import java.util.ArrayList;
import java.util.List;

final class CnpjMockLeadCatalog {

    private final String sourceName;

    CnpjMockLeadCatalog(String sourceName) {
        this.sourceName = sourceName;
    }

    List<DiscoveredLeadCandidate> search(String query, int limit) {
        List<DiscoveredLeadCandidate> companies = new ArrayList<>();
        String lower = query.toLowerCase();
        if (containsAny(lower, "tech", "tecnologia", "fintech", "startup")) {
            companies.addAll(techCompanies());
        }
        if (containsAny(lower, "agro", "agricultura", "fazenda", "rural")) {
            companies.addAll(agroCompanies());
        }
        if (containsAny(lower, "consultor", "consultoria", "empresa", "negocio", "servicos")) {
            companies.addAll(consultingCompanies());
        }
        if (containsAny(lower, "saude", "health", "educacao", "ensino", "telemedicina")) {
            companies.add(healthCompany());
        }
        if (companies.isEmpty()) {
            companies.add(defaultCompany());
        }
        return companies.stream().limit(limit).toList();
    }

    private List<DiscoveredLeadCandidate> techCompanies() {
        return List.of(
            candidate("TechSolutions Brasil Ltda", "https://techsolutions.com.br", "technology", "Software development and digital transformation services - CNPJ validated", "Sao Paulo, SP - Brazil", "contato@techsolutions.com.br", "cto@techsolutions.com.br"),
            candidate("InovaPay Fintech SA", "https://inovapay.com.br", "fintech", "Digital payment solutions for small businesses - CNPJ 12.345.678/0001-90", "Rio de Janeiro, RJ - Brazil", "info@inovapay.com.br", "founder@inovapay.com.br"),
            candidate("CloudBrasil Sistemas", "https://cloudbrasil.tech", "saas", "Cloud infrastructure and DevOps solutions - CNPJ 98.765.432/0001-10", "Florianopolis, SC - Brazil", "vendas@cloudbrasil.tech", "ceo@cloudbrasil.tech")
        );
    }

    private List<DiscoveredLeadCandidate> agroCompanies() {
        return List.of(
            candidate("AgroTech Mato Grosso Ltda", "https://agrotech-mt.com.br", "agtech", "Precision agriculture and IoT solutions for farms - CNPJ validated", "Cuiaba, MT - Brazil", "comercial@agrotech-mt.com.br", "diretor@agrotech-mt.com.br"),
            candidate("FarmData Analytics", "https://farmdata.agr.br", "agtech", "Data analytics and satellite monitoring for agriculture - CNPJ 11.222.333/0001-44", "Ribeirao Preto, SP - Brazil", "contato@farmdata.agr.br", "cto@farmdata.agr.br")
        );
    }

    private List<DiscoveredLeadCandidate> consultingCompanies() {
        return List.of(
            candidate("Consultoria Estrategica Brasil", "https://cestrategica.com.br", "consulting", "Business strategy and digital transformation consulting - Active CNPJ", "Brasilia, DF - Brazil", "contato@cestrategica.com.br", "diretor@cestrategica.com.br"),
            candidate("ServicosPro Ltda", "https://servicospro.net.br", "services", "Professional services and business automation - CNPJ 55.666.777/0001-88", "Porto Alegre, RS - Brazil", "info@servicospro.net.br", "gerente@servicospro.net.br")
        );
    }

    private DiscoveredLeadCandidate healthCompany() {
        return candidate("HealthTech Digital Ltda", "https://healthtech.med.br", "healthtech", "Telemedicine and digital health solutions - CNPJ verified", "Belo Horizonte, MG - Brazil", "contato@healthtech.med.br", "ceo@healthtech.med.br");
    }

    private DiscoveredLeadCandidate defaultCompany() {
        return candidate("Empresa Brasil Inovacao", "https://brasilempresas.com.br", "technology", "General technology solutions provider - CNPJ validated via CNPJ.ws", "Sao Paulo, SP - Brazil", "contato@brasilempresas.com.br");
    }

    private DiscoveredLeadCandidate candidate(
        String name,
        String website,
        String industry,
        String description,
        String location,
        String... contacts
    ) {
        return new DiscoveredLeadCandidate(name, website, industry, description, location, List.of(contacts), sourceName);
    }

    private boolean containsAny(String value, String... tokens) {
        for (String token : tokens) {
            if (value.contains(token)) {
                return true;
            }
        }
        return false;
    }
}
