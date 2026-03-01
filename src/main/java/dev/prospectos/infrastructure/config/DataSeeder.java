package dev.prospectos.infrastructure.config;

import dev.prospectos.api.CompanyDataService;
import dev.prospectos.api.ICPDataService;
import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.api.dto.ICPDto;
import dev.prospectos.api.dto.ScoreDTO;
import dev.prospectos.api.dto.request.CompanyCreateRequest;
import dev.prospectos.api.dto.request.ICPCreateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds the database with demonstration data for mock and development profiles.
 */
@Component
@Profile({"mock", "development"})
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final CompanyDataService companyService;
    private final ICPDataService icpService;

    public DataSeeder(CompanyDataService companyService, ICPDataService icpService) {
        this.companyService = companyService;
        this.icpService = icpService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seedDatabase() {
        // Always seed for demo purposes in mock/development
        log.info("Starting data seeding process for demo...");
        seedICPs();
        seedCompanies();
        assignRealisticScores();
        log.info("Database seeded successfully with {} companies and {} ICPs", 
                 companyService.findAllCompanies().size(),
                 icpService.findAllICPs().size());
    }

    private void seedICPs() {
        // ICP 1: Tech Startups
        ICPCreateRequest techICP = new ICPCreateRequest(
            "CTOs de Startups Tech",
            "Lideranças técnicas de startups de tecnologia em crescimento",
            List.of("technology", "fintech", "saas"),
            List.of("São Paulo", "Rio de Janeiro", "Belo Horizonte"),
            List.of("CTO", "Tech Lead", "VP Engineering", "Head of Engineering"),
            "Escalabilidade e arquitetura de sistemas",
            List.of("cloud", "microservices", "kubernetes", "react", "node.js"),
            10,
            100
        );
        icpService.createICP(techICP);

        // ICP 2: Agro Directors  
        ICPCreateRequest agroICP = new ICPCreateRequest(
            "Diretores do Agronegócio",
            "Lideranças de fazendas e cooperativas agrícolas",
            List.of("agribusiness", "agriculture", "farming"),
            List.of("Mato Grosso", "Rio Grande do Sul", "Goiás", "Minas Gerais"),
            List.of("Diretor", "Gerente Agrícola", "Coordenador de Fazenda"),
            "Tecnologia e produtividade agrícola",
            List.of("precision agriculture", "iot", "data analytics", "sustainability"),
            100,
            1000
        );
        icpService.createICP(agroICP);

        // ICP 3: Fintech Founders
        ICPCreateRequest fintechICP = new ICPCreateRequest(
            "Founders de Fintech",
            "Fundadores de fintechs em estágio de crescimento",
            List.of("fintech", "banking", "payments"),
            List.of("São Paulo", "Rio de Janeiro"),
            List.of("CEO", "Founder", "Co-founder"),
            "Regulamentação e crescimento no mercado financeiro",
            List.of("blockchain", "ai", "risk management", "compliance"),
            5,
            50
        );
        icpService.createICP(fintechICP);
    }

    private void seedCompanies() {
        // Tech Companies (30 empresas)
        seedTechCompanies();
        
        // Agronegócio (25 empresas)
        seedAgroCompanies();
        
        // Outros Setores (45 empresas)
        seedOtherSectorCompanies();
    }

    private void seedTechCompanies() {
        // Fintechs
        createCompany("Nubank", "fintech", "https://nubank.com.br", 
                     "Digital banking platform revolutionizing financial services", "Brazil", "São Paulo", "LARGE");
        createCompany("Stone Pagamentos", "fintech", "https://stone.com.br",
                     "Payment solutions for businesses", "Brazil", "São Paulo", "MEDIUM");
        createCompany("PagSeguro", "fintech", "https://pagseguro.uol.com.br",
                     "Digital payment and financial services", "Brazil", "São Paulo", "LARGE");
        createCompany("Creditas", "fintech", "https://creditas.com",
                     "Digital credit platform for secured loans", "Brazil", "São Paulo", "MEDIUM");

        // E-commerce & SaaS
        createCompany("Magazine Luiza", "ecommerce", "https://magazineluiza.com.br",
                     "Leading Brazilian e-commerce platform", "Brazil", "São Paulo", "LARGE");
        createCompany("RD Station", "saas", "https://rdstation.com",
                     "Marketing automation and CRM platform", "Brazil", "Florianópolis", "MEDIUM");
        createCompany("Pipefy", "saas", "https://pipefy.com",
                     "Process management and workflow automation", "Brazil", "Curitiba", "MEDIUM");
        createCompany("ContaAzul", "saas", "https://contaazul.com",
                     "ERP and financial management for SMBs", "Brazil", "Joinville", "MEDIUM");

        // Startups
        createCompany("Loggi", "logistics", "https://loggi.com",
                     "Last-mile delivery platform", "Brazil", "São Paulo", "MEDIUM");
        createCompany("iFood", "marketplace", "https://ifood.com.br",
                     "Food delivery platform", "Brazil", "São Paulo", "LARGE");
        createCompany("Gympass", "healthtech", "https://gympass.com",
                     "Corporate wellness platform", "Brazil", "São Paulo", "MEDIUM");
        createCompany("QuintoAndar", "proptech", "https://quintoandar.com.br",
                     "Real estate platform and property management", "Brazil", "São Paulo", "MEDIUM");

        // Tech Infrastructure
        createCompany("Locaweb", "hosting", "https://locaweb.com.br",
                     "Web hosting and cloud services", "Brazil", "São Paulo", "MEDIUM");
        createCompany("UOL Host", "hosting", "https://uolhost.uol.com.br",
                     "Cloud hosting and infrastructure services", "Brazil", "São Paulo", "MEDIUM");
        createCompany("Kinghost", "hosting", "https://kinghost.com.br",
                     "Web hosting and domain services", "Brazil", "Porto Alegre", "SMALL");
    }

    private void seedAgroCompanies() {
        // Grandes Produtores
        createCompany("SLC Agrícola", "agribusiness", "https://slcagricola.com.br",
                     "Large scale farming and grain production", "Brazil", "Primavera do Leste, MT", "LARGE");
        createCompany("BrasilAgro", "agribusiness", "https://brasileiragro.com.br",
                     "Agricultural real estate and farming", "Brazil", "São Paulo", "LARGE");
        createCompany("Cosan", "agribusiness", "https://cosan.com.br",
                     "Sugar, ethanol and energy production", "Brazil", "São Paulo", "LARGE");

        // Cooperativas
        createCompany("Coamo", "cooperative", "https://coamo.com.br",
                     "Agricultural cooperative for grain production", "Brazil", "Campo Mourão, PR", "LARGE");
        createCompany("Cocamar", "cooperative", "https://cocamar.com.br",
                     "Agricultural cooperative and agribusiness", "Brazil", "Maringá, PR", "LARGE");
        createCompany("Cooperalfa", "cooperative", "https://cooperalfa.com.br",
                     "Agricultural cooperative in Mato Grosso", "Brazil", "Lucas do Rio Verde, MT", "MEDIUM");

        // AgTech
        createCompany("Aegro", "agtech", "https://aegro.com.br",
                     "Farm management software platform", "Brazil", "Florianópolis", "SMALL");
        createCompany("Granular Brasil", "agtech", "https://granular.ag",
                     "Precision agriculture and farm analytics", "Brazil", "São Paulo", "MEDIUM");
        createCompany("Climate FieldView", "agtech", "https://climate.com",
                     "Digital agriculture and field monitoring", "Brazil", "São Paulo", "MEDIUM");

        // Sementes e Insumos
        createCompany("Biosul Sementes", "agribusiness", "https://biosul.com.br",
                     "Seed development and distribution", "Brazil", "Passo Fundo, RS", "SMALL");
        createCompany("Nidera Sementes", "agribusiness", "https://nidera.com.br",
                     "Agricultural seeds and crop protection", "Brazil", "Uberlândia, MG", "MEDIUM");
        createCompany("Pioneer Sementes", "agribusiness", "https://pioneer.com",
                     "Corn and soybean seeds", "Brazil", "Santa Cruz do Sul, RS", "MEDIUM");

        // Equipamentos Agrícolas
        createCompany("Jacto", "agrimachinery", "https://jacto.com.br",
                     "Agricultural machinery and equipment", "Brazil", "Pompéia, SP", "MEDIUM");
        createCompany("Stara", "agrimachinery", "https://stara.com.br",
                     "Agricultural machinery and precision farming", "Brazil", "Não-Me-Toque, RS", "MEDIUM");
    }

    private void seedOtherSectorCompanies() {
        // Saúde
        createCompany("Hospital Albert Einstein", "healthcare", "https://einstein.br",
                     "Leading private hospital and healthcare services", "Brazil", "São Paulo", "LARGE");
        createCompany("Fleury", "healthcare", "https://fleury.com.br",
                     "Laboratory and diagnostic medicine", "Brazil", "São Paulo", "LARGE");
        createCompany("Dasa", "healthcare", "https://dasa.com.br",
                     "Diagnostic medicine and healthcare network", "Brazil", "São Paulo", "LARGE");

        // Educação
        createCompany("Kroton Educacional", "education", "https://kroton.com.br",
                     "Higher education and online learning", "Brazil", "Belo Horizonte", "LARGE");
        createCompany("Descomplica", "edtech", "https://descomplica.com.br",
                     "Online education and test preparation", "Brazil", "Rio de Janeiro", "MEDIUM");
        createCompany("Alura", "edtech", "https://alura.com.br",
                     "Technology education and programming courses", "Brazil", "São Paulo", "MEDIUM");

        // Varejo
        createCompany("Lojas Americanas", "retail", "https://americanas.com.br",
                     "Retail chain and e-commerce platform", "Brazil", "Rio de Janeiro", "LARGE");
        createCompany("Casas Bahia", "retail", "https://casasbahia.com.br",
                     "Furniture and home appliances retail", "Brazil", "São Paulo", "LARGE");
        createCompany("Renner", "fashion", "https://lojasrenner.com.br",
                     "Fashion retail and clothing", "Brazil", "Porto Alegre", "LARGE");

        // Indústria
        createCompany("Embraer", "aerospace", "https://embraer.com",
                     "Aircraft manufacturer and aerospace company", "Brazil", "São José dos Campos, SP", "LARGE");
        createCompany("WEG", "manufacturing", "https://weg.net",
                     "Electric motors and industrial equipment", "Brazil", "Jaraguá do Sul, SC", "LARGE");
        createCompany("Suzano", "pulp", "https://suzano.com.br",
                     "Pulp and paper production", "Brazil", "São Paulo", "LARGE");

        // Energia
        createCompany("Petrobras", "energy", "https://petrobras.com.br",
                     "Oil and gas exploration and production", "Brazil", "Rio de Janeiro", "LARGE");
        createCompany("Vale", "mining", "https://vale.com",
                     "Mining and metals production", "Brazil", "Rio de Janeiro", "LARGE");

        // Consultoria
        createCompany("Accenture Brasil", "consulting", "https://accenture.com",
                     "Technology consulting and digital transformation", "Brazil", "São Paulo", "LARGE");
        createCompany("Deloitte Brasil", "consulting", "https://deloitte.com",
                     "Business consulting and audit services", "Brazil", "São Paulo", "LARGE");

        // Startups Emergentes
        createCompany("TechBrasil Solutions", "technology", "https://techbrasil.com.br",
                     "Custom software development and IT consulting", "Brazil", "São Paulo", "STARTUP");
        createCompany("InnovaCorp Digital", "consulting", "https://inovacorp.com.br",
                     "Digital transformation consultancy", "Brazil", "Rio de Janeiro", "STARTUP");
        createCompany("AgroTech Innovations", "agtech", "https://agrotech.startup",
                     "IoT solutions for precision agriculture", "Brazil", "Piracicaba, SP", "STARTUP");
        createCompany("HealthTech Solutions", "healthtech", "https://healthtech.startup",
                     "Telemedicine platform for remote consultations", "Brazil", "Belo Horizonte", "STARTUP");
        createCompany("EduTech Learning", "edtech", "https://edutech.learning",
                     "AI-powered personalized learning platform", "Brazil", "Florianópolis", "STARTUP");
    }

    private void createCompany(String name, String industry, String website, 
                              String description, String country, String city, String size) {
        try {
            CompanyCreateRequest request = new CompanyCreateRequest(
                name, industry, website, description, country, city, size
            );
            companyService.createCompany(request);
        } catch (Exception e) {
            log.warn("Failed to create company {}: {}", name, e.getMessage());
        }
    }

    private void assignRealisticScores() {
        List<CompanyDTO> companies = companyService.findAllCompanies();
        
        for (CompanyDTO company : companies) {
            int score = calculateRealisticScore(company);
            String reasoning = generateScoreReasoning(company, score);
            String category = categorizeScore(score);
            ScoreDTO scoreDTO = new ScoreDTO(score, category, reasoning);
            companyService.updateCompanyScore(company.id(), scoreDTO);
        }
    }

    private int calculateRealisticScore(CompanyDTO company) {
        int baseScore = 60; // Base score
        
        // Industry bonus
        if (company.industry().equals("fintech")) baseScore += 15;
        if (company.industry().equals("technology")) baseScore += 12;
        if (company.industry().equals("agtech")) baseScore += 10;
        if (company.industry().equals("saas")) baseScore += 10;
        if (company.industry().equals("agribusiness")) baseScore += 5;
        if (company.industry().equals("consulting")) baseScore += 7;
        
        // Size bonus - Startups have higher potential
        // Size information is not directly available in CompanyDTO
        // We'll infer from employee count or company name
        if (company.name().toLowerCase().contains("startup") || 
            (company.employeeCount() != null && company.employeeCount() < 50)) {
            baseScore += 20;
        } else if (company.employeeCount() != null && company.employeeCount() < 200) {
            baseScore += 15;
        } else if (company.employeeCount() != null && company.employeeCount() < 1000) {
            baseScore += 10;
        } else {
            baseScore += 5;
        }
        
        // Location bonus
        if (company.location() != null) {
            if (company.location().contains("São Paulo")) baseScore += 10;
            if (company.location().contains("Rio de Janeiro")) baseScore += 8;
            if (company.location().contains("Florianópolis")) baseScore += 6;
            if (company.location().contains("Belo Horizonte")) baseScore += 6;
        }
        
        // Technology company bonus
        if (company.name().toLowerCase().contains("tech") ||
            company.description().toLowerCase().contains("digital") ||
            company.description().toLowerCase().contains("platform")) {
            baseScore += 8;
        }
        
        // Add some randomness for realistic variation
        baseScore += (int)(Math.random() * 10 - 5);
        
        return Math.min(Math.max(baseScore, 45), 95); // Clamp between 45-95
    }

    private String generateScoreReasoning(CompanyDTO company, int score) {
        StringBuilder reasoning = new StringBuilder();
        
        if (score >= 80) {
            reasoning.append("HIGH PRIORITY: ");
        } else if (score >= 65) {
            reasoning.append("MEDIUM PRIORITY: ");
        } else {
            reasoning.append("LOW PRIORITY: ");
        }
        
        // Industry reasoning
        switch (company.industry()) {
            case "fintech", "technology", "saas" -> 
                reasoning.append("Strong tech industry fit. ");
            case "agtech" -> 
                reasoning.append("Growing AgTech sector with digital adoption. ");
            case "agribusiness" -> 
                reasoning.append("Traditional agro with modernization potential. ");
            case "consulting" -> 
                reasoning.append("Service industry with technology needs. ");
            default -> 
                reasoning.append("Industry analysis complete. ");
        }
        
        // Size reasoning
        // Size reasoning based on employee count or company name
        if (company.name().toLowerCase().contains("startup") || 
            (company.employeeCount() != null && company.employeeCount() < 50)) {
            reasoning.append("High growth potential, early-stage innovation.");
        } else if (company.employeeCount() != null && company.employeeCount() < 200) {
            reasoning.append("Agile company, good decision-making speed.");
        } else if (company.employeeCount() != null && company.employeeCount() < 1000) {
            reasoning.append("Established operations with growth capacity.");
        } else {
            reasoning.append("Stable enterprise, complex decision process.");
        }
        
        // Location context
        if (company.location() != null && company.location().contains("São Paulo")) {
            reasoning.append(" Located in Brazil's tech hub.");
        }
        
        return reasoning.toString();
    }

    private String categorizeScore(int score) {
        if (score >= 80) {
            return "HOT";
        } else if (score >= 65) {
            return "WARM";
        } else {
            return "COLD";
        }
    }
}