package dev.prospectos.infrastructure.service.inmemory;

import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.api.dto.ICPDto;

import java.util.List;
import java.util.Map;

final class InMemorySeedData {

    private InMemorySeedData() {
    }

    static Map<Long, CompanyDTO> companies() {
        return Map.of(
            1L, company(1L, "TechCorp", "Software", "https://techcorp.com", "Leading software company", 150, "San Francisco, CA"),
            2L, company(2L, "CloudTech Solutions", "Software", "https://cloudtech.com", "Cloud infrastructure specialists", 220, "Austin, TX"),
            3L, company(3L, "Local Restaurant", "Food & Beverage", "https://localrestaurant.com", "Neighborhood dining spot", 25, "Curitiba, BR"),
            4L, company(4L, "TechStart1", "Software", "https://techstart1.com", "Early-stage software startup", 40, "Sao Paulo, BR"),
            5L, company(5L, "TechStart2", "Software", "https://techstart2.com", "Growing SaaS platform", 55, "Toronto, CA"),
            6L, company(6L, "TechStart3", "Software", "https://techstart3.com", "Product-led startup", 65, "Miami, FL"),
            7L, company(7L, "MinimalCorp", "Unknown", "https://minimal.com", "Minimal company profile", 10, "Remote")
        );
    }

    static ICPDto icp() {
        return new ICPDto(
            1L,
            "DevOps Teams",
            "Target companies with active DevOps practices",
            List.of("Software", "Technology", "SaaS"),
            List.of("North America", "Europe", "Brazil"),
            List.of("Docker", "Kubernetes", "AWS", "Jenkins"),
            50,
            500,
            List.of("CTO", "DevOps Engineer", "Platform Engineer"),
            "DevOps transformation and cloud migration"
        );
    }

    static List<Long> icpCompanyIds() {
        return List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L);
    }

    private static CompanyDTO company(Long id, String name, String industry, String website, String description,
                                      Integer employeeCount, String location) {
        return new CompanyDTO(id, name, industry, website, description, employeeCount, location, null);
    }
}
