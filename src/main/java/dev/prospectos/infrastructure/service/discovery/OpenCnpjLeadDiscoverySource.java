package dev.prospectos.infrastructure.service.discovery;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class OpenCnpjLeadDiscoverySource implements LeadDiscoverySource {

    private static final Logger log = LoggerFactory.getLogger(OpenCnpjLeadDiscoverySource.class);
    private static final String SOURCE_NAME = "open-cnpj";
    private static final String CNPJ_ENDPOINT = "https://kitana.opencnpj.com/cnpj/{cnpj}";
    private static final Pattern CNPJ_PATTERN = Pattern.compile("\\d{2}\\.?\\d{3}\\.?\\d{3}/?\\d{4}-?\\d{2}|\\d{14}");

    private final RestTemplate restTemplate;
    private final CnpjMockLeadCatalog mockLeadCatalog = new CnpjMockLeadCatalog(SOURCE_NAME);
    private final OpenCnpjCandidateMapper candidateMapper = new OpenCnpjCandidateMapper(SOURCE_NAME);

    public OpenCnpjLeadDiscoverySource(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @Override
    public String sourceName() {
        return SOURCE_NAME;
    }

    @Override
    public List<DiscoveredLeadCandidate> discover(DiscoveryContext context) {
        int limit = Math.max(context.limit(), 0);
        if (limit == 0) {
            return List.of();
        }

        try {
            Optional<String> cnpj = extractCnpjDigits(context.query());
            if (cnpj.isPresent()) {
                Optional<DiscoveredLeadCandidate> candidate = fetchCandidate(cnpj.get());
                if (candidate.isPresent()) {
                    return List.of(candidate.get());
                }
            }
        } catch (Exception e) {
            log.warn("OpenCNPJ search failed, returning mock data: {}", e.getMessage());
        }

        return mockLeadCatalog.search(context.query(), limit);
    }

    private Optional<DiscoveredLeadCandidate> fetchCandidate(String cnpj) {
        ResponseEntity<OpenCnpjResponse> response = restTemplate.getForEntity(CNPJ_ENDPOINT, OpenCnpjResponse.class, cnpj);
        if (!response.getStatusCode().is2xxSuccessful()) {
            return Optional.empty();
        }

        OpenCnpjResponse body = response.getBody();
        if (body == null || !body.isSuccessful()) {
            return Optional.empty();
        }

        return candidateMapper.map(body.data());
    }

    private Optional<String> extractCnpjDigits(String query) {
        if (query == null || query.isBlank()) {
            return Optional.empty();
        }

        Matcher matcher = CNPJ_PATTERN.matcher(query);
        if (!matcher.find()) {
            return Optional.empty();
        }

        String digits = matcher.group().replaceAll("\\D", "");
        return digits.length() == 14 ? Optional.of(digits) : Optional.empty();
    }
}
