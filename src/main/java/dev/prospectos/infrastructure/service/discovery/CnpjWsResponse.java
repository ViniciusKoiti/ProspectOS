package dev.prospectos.infrastructure.service.discovery;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
class CnpjWsResponse {

    public int status;
    public String nome;
    public String fantasia;
    public String email;
    public String situacao;
    public String logradouro;
    public String municipio;
    public String uf;

    @JsonProperty("atividade_principal")
    public List<AtividadeResponse> atividadePrincipal;

    static class AtividadeResponse {
        public String code;
        public String text;
    }
}
