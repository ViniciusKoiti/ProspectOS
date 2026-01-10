package dev.prospectos.infrastructure.api.companies;

import dev.prospectos.api.CompanyDataService;
import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.api.dto.ScoreDTO;
import dev.prospectos.api.dto.request.CompanyCreateRequest;
import dev.prospectos.api.dto.request.CompanyUpdateRequest;
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
 * Company management endpoints for the MVP.
 */
@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyDataService companyDataService;

    public CompanyController(CompanyDataService companyDataService) {
        this.companyDataService = companyDataService;
    }

    @GetMapping
    public ResponseEntity<List<CompanyDTO>> listCompanies() {
        return ResponseEntity.ok(companyDataService.findAllCompanies());
    }

    @PostMapping
    public ResponseEntity<CompanyDTO> createCompany(@Valid @RequestBody CompanyCreateRequest request) {
        CompanyDTO company = companyDataService.createCompany(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(company);
    }

    @GetMapping("/{companyId}")
    public ResponseEntity<CompanyDTO> getCompany(@PathVariable Long companyId) {
        CompanyDTO company = companyDataService.findCompany(companyId);
        if (company == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found");
        }
        return ResponseEntity.ok(company);
    }

    @PutMapping("/{companyId}")
    public ResponseEntity<CompanyDTO> updateCompany(
        @PathVariable Long companyId,
        @Valid @RequestBody CompanyUpdateRequest request
    ) {
        CompanyDTO company = companyDataService.updateCompany(companyId, request);
        if (company == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found");
        }
        return ResponseEntity.ok(company);
    }

    @DeleteMapping("/{companyId}")
    public ResponseEntity<Void> deleteCompany(@PathVariable Long companyId) {
        if (!companyDataService.deleteCompany(companyId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found");
        }
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{companyId}/score")
    public ResponseEntity<Void> updateScore(
        @PathVariable Long companyId,
        @Valid @RequestBody ScoreDTO score
    ) {
        companyDataService.updateCompanyScore(companyId, score);
        return ResponseEntity.noContent().build();
    }
}
