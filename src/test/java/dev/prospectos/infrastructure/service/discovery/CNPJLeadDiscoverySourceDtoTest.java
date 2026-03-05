package dev.prospectos.infrastructure.service.discovery;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CNPJLeadDiscoverySourceDtoTest {

    @Test
    void atividadeResponseExposesPublicFields() {
        CNPJLeadDiscoverySource.CNPJResponse.AtividadeResponse atividade = new CNPJLeadDiscoverySource.CNPJResponse.AtividadeResponse();
        atividade.code = "6201";
        atividade.text = "Desenvolvimento de software";

        assertThat(atividade.code).isEqualTo("6201");
        assertThat(atividade.text).isEqualTo("Desenvolvimento de software");
    }
}
