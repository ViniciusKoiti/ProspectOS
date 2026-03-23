package dev.prospectos.infrastructure.service.discovery;

import java.util.List;
import java.util.Optional;

final class OpenCnpjCandidateMapper {

    private final String sourceName;

    OpenCnpjCandidateMapper(String sourceName) {
        this.sourceName = sourceName;
    }

    Optional<DiscoveredLeadCandidate> map(OpenCnpjCompanyData data) {
        if (data == null) {
            return Optional.empty();
        }

        String name = firstNonBlank(data.nomeFantasia(), data.razaoSocial());
        if (name == null) {
            return Optional.empty();
        }

        return Optional.of(new DiscoveredLeadCandidate(
            name,
            null,
            toIndustry(data.cnaes()),
            toDescription(data),
            toLocation(data),
            contacts(data.email()),
            sourceName
        ));
    }

    private String toIndustry(List<OpenCnpjCnae> cnaes) {
        if (cnaes == null || cnaes.isEmpty() || cnaes.getFirst() == null) {
            return "Other";
        }

        String description = text(cnaes.getFirst().descricao());
        return description == null ? "Other" : description;
    }

    private String toDescription(OpenCnpjCompanyData data) {
        String cnpj = text(data.cnpj());
        String legalName = text(data.razaoSocial());
        String status = text(data.situacaoCadastral());

        StringBuilder builder = new StringBuilder("OpenCNPJ");
        if (legalName != null) {
            builder.append(" legalName=").append(legalName);
        }
        if (cnpj != null) {
            builder.append(" cnpj=").append(cnpj);
        }
        if (status != null) {
            builder.append(" status=").append(status);
        }

        return builder.toString();
    }

    private String toLocation(OpenCnpjCompanyData data) {
        String city = text(data.municipio());
        String state = text(data.uf());
        if (city == null && state == null) {
            return "Brazil";
        }
        if (city == null) {
            return state + " - Brazil";
        }
        if (state == null) {
            return city + " - Brazil";
        }
        return city + ", " + state + " - Brazil";
    }

    private List<String> contacts(String email) {
        String normalizedEmail = text(email);
        return normalizedEmail == null ? List.of() : List.of(normalizedEmail);
    }

    private String firstNonBlank(String first, String second) {
        String firstValue = text(first);
        if (firstValue != null) {
            return firstValue;
        }
        return text(second);
    }

    private String text(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}
