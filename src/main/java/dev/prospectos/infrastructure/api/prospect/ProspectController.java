package dev.prospectos.infrastructure.api.prospect;

import dev.prospectos.api.dto.ProspectEnrichRequest;
import dev.prospectos.api.dto.ProspectEnrichResponse;
import dev.prospectos.infrastructure.service.prospect.ProspectEnrichmentFacade;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/prospect")
public class ProspectController {

    private final ProspectEnrichmentFacade enrichmentFacade;

    public ProspectController(ProspectEnrichmentFacade enrichmentFacade) {
        this.enrichmentFacade = enrichmentFacade;
    }

    @PostMapping("/enrich")
    public ResponseEntity<ProspectEnrichResponse> enrich(@RequestBody ProspectEnrichRequest request) {
        ProspectEnrichResponse response = enrichmentFacade.enrich(request);
        return ResponseEntity.ok(response);
    }
}
