package dev.prospectos.infrastructure.config;

import java.util.List;

import dev.prospectos.api.dto.request.CompanyCreateRequest;

final class DataSeederCompanyRequests {

    private DataSeederCompanyRequests() {
    }

    static List<CompanyCreateRequest> requests() {
        return List.of(
            request("Nubank", "fintech", "https://nubank.com.br", "Digital banking platform revolutionizing financial services", "Brazil", "São Paulo", "LARGE"),
            request("Stone Pagamentos", "fintech", "https://stone.com.br", "Payment solutions for businesses", "Brazil", "São Paulo", "MEDIUM"),
            request("PagSeguro", "fintech", "https://pagseguro.uol.com.br", "Digital payment and financial services", "Brazil", "São Paulo", "LARGE"),
            request("Creditas", "fintech", "https://creditas.com", "Digital credit platform for secured loans", "Brazil", "São Paulo", "MEDIUM"),
            request("Magazine Luiza", "ecommerce", "https://magazineluiza.com.br", "Leading Brazilian e-commerce platform", "Brazil", "São Paulo", "LARGE"),
            request("RD Station", "saas", "https://rdstation.com", "Marketing automation and CRM platform", "Brazil", "Florianópolis", "MEDIUM"),
            request("Pipefy", "saas", "https://pipefy.com", "Process management and workflow automation", "Brazil", "Curitiba", "MEDIUM"),
            request("ContaAzul", "saas", "https://contaazul.com", "ERP and financial management for SMBs", "Brazil", "Joinville", "MEDIUM"),
            request("Loggi", "logistics", "https://loggi.com", "Last-mile delivery platform", "Brazil", "São Paulo", "MEDIUM"),
            request("iFood", "marketplace", "https://ifood.com.br", "Food delivery platform", "Brazil", "São Paulo", "LARGE"),
            request("Gympass", "healthtech", "https://gympass.com", "Corporate wellness platform", "Brazil", "São Paulo", "MEDIUM"),
            request("QuintoAndar", "proptech", "https://quintoandar.com.br", "Real estate platform and property management", "Brazil", "São Paulo", "MEDIUM"),
            request("Locaweb", "hosting", "https://locaweb.com.br", "Web hosting and cloud services", "Brazil", "São Paulo", "MEDIUM"),
            request("UOL Host", "hosting", "https://uolhost.uol.com.br", "Cloud hosting and infrastructure services", "Brazil", "São Paulo", "MEDIUM"),
            request("Kinghost", "hosting", "https://kinghost.com.br", "Web hosting and domain services", "Brazil", "Porto Alegre", "SMALL"),
            request("SLC Agrícola", "agribusiness", "https://slcagricola.com.br", "Large scale farming and grain production", "Brazil", "Primavera do Leste, MT", "LARGE"),
            request("BrasilAgro", "agribusiness", "https://brasileiragro.com.br", "Agricultural real estate and farming", "Brazil", "São Paulo", "LARGE"),
            request("Cosan", "agribusiness", "https://cosan.com.br", "Sugar, ethanol and energy production", "Brazil", "São Paulo", "LARGE"),
            request("Coamo", "cooperative", "https://coamo.com.br", "Agricultural cooperative for grain production", "Brazil", "Campo Mourão, PR", "LARGE"),
            request("Cocamar", "cooperative", "https://cocamar.com.br", "Agricultural cooperative and agribusiness", "Brazil", "Maringá, PR", "LARGE"),
            request("Cooperalfa", "cooperative", "https://cooperalfa.com.br", "Agricultural cooperative in Mato Grosso", "Brazil", "Lucas do Rio Verde, MT", "MEDIUM"),
            request("Aegro", "agtech", "https://aegro.com.br", "Farm management software platform", "Brazil", "Florianópolis", "SMALL"),
            request("Granular Brasil", "agtech", "https://granular.ag", "Precision agriculture and farm analytics", "Brazil", "São Paulo", "MEDIUM"),
            request("Climate FieldView", "agtech", "https://climate.com", "Digital agriculture and field monitoring", "Brazil", "São Paulo", "MEDIUM"),
            request("Biosul Sementes", "agribusiness", "https://biosul.com.br", "Seed development and distribution", "Brazil", "Passo Fundo, RS", "SMALL"),
            request("Nidera Sementes", "agribusiness", "https://nidera.com.br", "Agricultural seeds and crop protection", "Brazil", "Uberlândia, MG", "MEDIUM"),
            request("Pioneer Sementes", "agribusiness", "https://pioneer.com", "Corn and soybean seeds", "Brazil", "Santa Cruz do Sul, RS", "MEDIUM"),
            request("Jacto", "agrimachinery", "https://jacto.com.br", "Agricultural machinery and equipment", "Brazil", "Pompéia, SP", "MEDIUM"),
            request("Stara", "agrimachinery", "https://stara.com.br", "Agricultural machinery and precision farming", "Brazil", "Não-Me-Toque, RS", "MEDIUM"),
            request("Hospital Albert Einstein", "healthcare", "https://einstein.br", "Leading private hospital and healthcare services", "Brazil", "São Paulo", "LARGE"),
            request("Fleury", "healthcare", "https://fleury.com.br", "Laboratory and diagnostic medicine", "Brazil", "São Paulo", "LARGE"),
            request("Dasa", "healthcare", "https://dasa.com.br", "Diagnostic medicine and healthcare network", "Brazil", "São Paulo", "LARGE"),
            request("Kroton Educacional", "education", "https://kroton.com.br", "Higher education and online learning", "Brazil", "Belo Horizonte", "LARGE"),
            request("Descomplica", "edtech", "https://descomplica.com.br", "Online education and test preparation", "Brazil", "Rio de Janeiro", "MEDIUM"),
            request("Alura", "edtech", "https://alura.com.br", "Technology education and programming courses", "Brazil", "São Paulo", "MEDIUM"),
            request("Lojas Americanas", "retail", "https://americanas.com.br", "Retail chain and e-commerce platform", "Brazil", "Rio de Janeiro", "LARGE"),
            request("Casas Bahia", "retail", "https://casasbahia.com.br", "Furniture and home appliances retail", "Brazil", "São Paulo", "LARGE"),
            request("Renner", "fashion", "https://lojasrenner.com.br", "Fashion retail and clothing", "Brazil", "Porto Alegre", "LARGE"),
            request("Embraer", "aerospace", "https://embraer.com", "Aircraft manufacturer and aerospace company", "Brazil", "São José dos Campos, SP", "LARGE"),
            request("WEG", "manufacturing", "https://weg.net", "Electric motors and industrial equipment", "Brazil", "Jaraguá do Sul, SC", "LARGE"),
            request("Suzano", "pulp", "https://suzano.com.br", "Pulp and paper production", "Brazil", "São Paulo", "LARGE"),
            request("Petrobras", "energy", "https://petrobras.com.br", "Oil and gas exploration and production", "Brazil", "Rio de Janeiro", "LARGE"),
            request("Vale", "mining", "https://vale.com", "Mining and metals production", "Brazil", "Rio de Janeiro", "LARGE"),
            request("Accenture Brasil", "consulting", "https://accenture.com", "Technology consulting and digital transformation", "Brazil", "São Paulo", "LARGE"),
            request("Deloitte Brasil", "consulting", "https://deloitte.com", "Business consulting and audit services", "Brazil", "São Paulo", "LARGE"),
            request("TechBrasil Solutions", "technology", "https://techbrasil.com.br", "Custom software development and IT consulting", "Brazil", "São Paulo", "STARTUP"),
            request("InnovaCorp Digital", "consulting", "https://inovacorp.com.br", "Digital transformation consultancy", "Brazil", "Rio de Janeiro", "STARTUP"),
            request("AgroTech Innovations", "agtech", "https://agrotech.startup", "IoT solutions for precision agriculture", "Brazil", "Piracicaba, SP", "STARTUP"),
            request("HealthTech Solutions", "healthtech", "https://healthtech.startup", "Telemedicine platform for remote consultations", "Brazil", "Belo Horizonte", "STARTUP"),
            request("EduTech Learning", "edtech", "https://edutech.learning", "AI-powered personalized learning platform", "Brazil", "Florianópolis", "STARTUP")
        );
    }

    private static CompanyCreateRequest request(
        String name,
        String industry,
        String website,
        String description,
        String country,
        String city,
        String size
    ) {
        return new CompanyCreateRequest(name, industry, website, description, country, city, size);
    }
}
