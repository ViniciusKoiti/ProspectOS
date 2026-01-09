package dev.prospectos.infrastructure.api;

import dev.prospectos.ai.service.ProspectorAIService;
import dev.prospectos.core.domain.Company;
import dev.prospectos.core.domain.Website;
import dev.prospectos.infrastructure.api.dto.ProspectEnrichRequest;
import dev.prospectos.infrastructure.api.dto.ProspectEnrichResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/prospect")
public class ProspectController {

    private final ProspectorAIService prospectorAIService;

    public ProspectController(ProspectorAIService prospectorAIService) {
        this.prospectorAIService = prospectorAIService;
    }

    @PostMapping("/enrich")
    public ResponseEntity<ProspectEnrichResponse> enrich(@RequestBody ProspectEnrichRequest request) {
        String name = request.name() != null ? request.name().trim() : "";
        String website = request.website() != null ? request.website().trim() : "";

        if (name.isEmpty() || website.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        String industry = request.industry() != null ? request.industry().trim() : null;
        Company company = Company.create(name, Website.of(website), industry);
        String analysis = prospectorAIService.enrichCompanyWithAI(company);

        return ResponseEntity.ok(new ProspectEnrichResponse(
            company.getName(),
            company.getWebsite().getUrl(),
            company.getIndustry(),
            analysis
        ));
    }
}
