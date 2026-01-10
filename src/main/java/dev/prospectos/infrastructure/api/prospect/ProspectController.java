package dev.prospectos.infrastructure.api.prospect;

import dev.prospectos.api.ProspectEnrichService;
import dev.prospectos.api.dto.ProspectEnrichRequest;
import dev.prospectos.api.dto.ProspectEnrichResponse;
import dev.prospectos.core.domain.Company;
import dev.prospectos.core.domain.Website;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/prospect")
public class ProspectController {

    private final ProspectEnrichService prospectEnrichService;

    public ProspectController(ProspectEnrichService prospectEnrichService) {
        this.prospectEnrichService = prospectEnrichService;
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
        String analysis = prospectEnrichService.enrichCompany(company);

        return ResponseEntity.ok(new ProspectEnrichResponse(
            company.getName(),
            company.getWebsite().getUrl(),
            company.getIndustry(),
            analysis
        ));
    }
}
