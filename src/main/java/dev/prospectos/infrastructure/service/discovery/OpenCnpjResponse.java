package dev.prospectos.infrastructure.service.discovery;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
record OpenCnpjResponse(
    Boolean success,
    String message,
    OpenCnpjCompanyData data
) {
    boolean isSuccessful() {
        return Boolean.TRUE.equals(success) && data != null;
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
record OpenCnpjCompanyData(
    String cnpj,
    String situacaoCadastral,
    String razaoSocial,
    String nomeFantasia,
    String email,
    String telefone,
    String logradouro,
    String numero,
    String complemento,
    String bairro,
    String municipio,
    String uf,
    List<OpenCnpjCnae> cnaes
) {
}

@JsonIgnoreProperties(ignoreUnknown = true)
record OpenCnpjCnae(
    String cnae,
    String descricao
) {
}
