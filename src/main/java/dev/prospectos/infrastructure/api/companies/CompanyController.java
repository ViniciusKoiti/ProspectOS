package dev.prospectos.infrastructure.api.companies;

import dev.prospectos.api.CompanyDataService;
import dev.prospectos.api.dto.CompanyDTO;
import dev.prospectos.api.dto.CompanyContactDTO;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
@RestController
@RequestMapping("/api/companies")
public class CompanyController {
    private final CompanyDataService companyDataService;
    private final CompanyListFilter companyListFilter;
    public CompanyController(CompanyDataService companyDataService) {
        this.companyDataService = companyDataService;
        this.companyListFilter = new CompanyListFilter();
    }
    @GetMapping
    public ResponseEntity<List<CompanyDTO>> listCompanies(
        @RequestParam(required = false) String query,
        @RequestParam(required = false) String industry,
        @RequestParam(required = false) String location,
        @RequestParam(required = false) Double minScore,
        @RequestParam(required = false) Double maxScore,
        @RequestParam(required = false) Boolean hasContact,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size
    ) {
        CompanyListFilter.Result filteredCompanies = companyListFilter.apply(
            companyDataService.findAllCompanies(),
            query,
            industry,
            location,
            minScore,
            maxScore,
            hasContact,
            page,
            size
        );
        return ResponseEntity.ok()
            .header("X-Total-Count", String.valueOf(filteredCompanies.totalItems()))
            .body(filteredCompanies.items());
    }

    @PostMapping
    public ResponseEntity<CompanyDTO> createCompany(@Valid @RequestBody CompanyCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(companyDataService.createCompany(request));
    }
    @GetMapping("/{companyId}")
    public ResponseEntity<CompanyDTO> getCompany(@PathVariable Long companyId) {
        return ResponseEntity.ok(requireCompany(companyId));
    }
    @GetMapping("/{companyId}/contacts")
    public ResponseEntity<List<CompanyContactDTO>> getCompanyContacts(@PathVariable Long companyId) {
        requireCompany(companyId);
        return ResponseEntity.ok(companyDataService.findCompanyContacts(companyId));
    }
    @PutMapping("/{companyId}")
    public ResponseEntity<CompanyDTO> updateCompany(@PathVariable Long companyId, @Valid @RequestBody CompanyUpdateRequest request) {
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
    public ResponseEntity<Void> updateScore(@PathVariable Long companyId, @Valid @RequestBody ScoreDTO score) {
        companyDataService.updateCompanyScore(companyId, score);
        return ResponseEntity.noContent().build();
    }
    private CompanyDTO requireCompany(Long companyId) {
        CompanyDTO company = companyDataService.findCompany(companyId);
        if (company == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found");
        }
        return company;
    }
}
