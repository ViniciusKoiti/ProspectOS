package dev.prospectos.infrastructure.config;

import java.util.List;

import dev.prospectos.api.dto.request.ICPCreateRequest;

final class DataSeederIcpRequests {

    private DataSeederIcpRequests() {
    }

    static List<ICPCreateRequest> requests() {
        return List.of(
            new ICPCreateRequest("CTOs de Startups Tech", "Lideranças técnicas de startups de tecnologia em crescimento", List.of("technology", "fintech", "saas"), List.of("São Paulo", "Rio de Janeiro", "Belo Horizonte"), List.of("CTO", "Tech Lead", "VP Engineering", "Head of Engineering"), "Escalabilidade e arquitetura de sistemas", List.of("cloud", "microservices", "kubernetes", "react", "node.js"), 10, 100),
            new ICPCreateRequest("Diretores do Agronegócio", "Lideranças de fazendas e cooperativas agrícolas", List.of("agribusiness", "agriculture", "farming"), List.of("Mato Grosso", "Rio Grande do Sul", "Goiás", "Minas Gerais"), List.of("Diretor", "Gerente Agrícola", "Coordenador de Fazenda"), "Tecnologia e produtividade agrícola", List.of("precision agriculture", "iot", "data analytics", "sustainability"), 100, 1000),
            new ICPCreateRequest("Founders de Fintech", "Fundadores de fintechs em estágio de crescimento", List.of("fintech", "banking", "payments"), List.of("São Paulo", "Rio de Janeiro"), List.of("CEO", "Founder", "Co-founder"), "Regulamentação e crescimento no mercado financeiro", List.of("blockchain", "ai", "risk management", "compliance"), 5, 50)
        );
    }
}
