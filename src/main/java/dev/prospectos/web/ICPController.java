package dev.prospectos.web;

import dev.prospectos.api.CompanyDataService;
import dev.prospectos.api.ICPDataService;
import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.api.dto.ICPDto;
import dev.prospectos.api.dto.request.ICPCreateRequest;
import dev.prospectos.api.dto.request.ICPUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * ICP management endpoints for the MVP.
 */
@RestController
@RequestMapping("/api/icps")
public class ICPController {

    private final ICPDataService icpDataService;
    private final CompanyDataService companyDataService;

    public ICPController(ICPDataService icpDataService, CompanyDataService companyDataService) {
        this.icpDataService = icpDataService;
        this.companyDataService = companyDataService;
    }

    @GetMapping
    public ResponseEntity<List<ICPDto>> listICPs() {
        return ResponseEntity.ok(icpDataService.findAllICPs());
    }

    @PostMapping
    public ResponseEntity<ICPDto> createICP(@Valid @RequestBody ICPCreateRequest request) {
        ICPDto icp = icpDataService.createICP(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(icp);
    }

    @GetMapping("/{icpId}")
    public ResponseEntity<ICPDto> getICP(@PathVariable Long icpId) {
        ICPDto icp = icpDataService.findICP(icpId);
        if (icp == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ICP not found");
        }
        return ResponseEntity.ok(icp);
    }

    @PutMapping("/{icpId}")
    public ResponseEntity<ICPDto> updateICP(
        @PathVariable Long icpId,
        @Valid @RequestBody ICPUpdateRequest request
    ) {
        ICPDto icp = icpDataService.updateICP(icpId, request);
        if (icp == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ICP not found");
        }
        return ResponseEntity.ok(icp);
    }

    @DeleteMapping("/{icpId}")
    public ResponseEntity<Void> deleteICP(@PathVariable Long icpId) {
        if (!icpDataService.deleteICP(icpId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ICP not found");
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{icpId}/companies")
    public ResponseEntity<List<CompanyDTO>> listCompaniesByICP(@PathVariable Long icpId) {
        return ResponseEntity.ok(companyDataService.findCompaniesByICP(icpId));
    }
}
