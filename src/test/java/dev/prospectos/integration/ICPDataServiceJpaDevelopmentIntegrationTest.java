package dev.prospectos.integration;

import dev.prospectos.api.dto.ICPDto;
import dev.prospectos.api.dto.request.ICPCreateRequest;
import dev.prospectos.api.dto.request.ICPUpdateRequest;
import dev.prospectos.support.PostgresIntegrationTestBase;
import dev.prospectos.infrastructure.adapter.ICPRepositoryAdapter;
import dev.prospectos.infrastructure.service.jpa.ICPDataServiceJpa;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles({"development", "test-pg"})
@Import({
    ICPRepositoryAdapter.class,
    ICPDataServiceJpa.class
})
class ICPDataServiceJpaDevelopmentIntegrationTest extends PostgresIntegrationTestBase {

    @Autowired
    private ICPDataServiceJpa icpDataService;

    @Test
    void createFindUpdateDeleteAndListIcpThroughJpa() {
        ICPDto created = icpDataService.createICP(new ICPCreateRequest(
            "B2B SaaS",
            "Targets mid-market SaaS companies",
            List.of("SaaS", "Technology"),
            List.of("BR", "US"),
            List.of("CTO", "Head of Sales"),
            "Revenue acceleration",
            null,
            null,
            null
        ));

        assertThat(created.id()).isNotNull();
        assertThat(created.targetIndustries()).containsExactly("SaaS", "Technology");

        ICPDto found = icpDataService.findICP(created.id());

        assertThat(found).isNotNull();
        assertThat(found.name()).isEqualTo("B2B SaaS");

        ICPDto updated = icpDataService.updateICP(created.id(), new ICPUpdateRequest(
            "B2B SaaS Updated",
            "Updated description",
            List.of("Technology"),
            List.of("US"),
            List.of("VP Sales"),
            "Outbound optimization",
            null,
            null,
            null
        ));

        assertThat(updated).isNotNull();
        assertThat(updated.name()).isEqualTo("B2B SaaS Updated");
        assertThat(updated.description()).isEqualTo("Updated description");
        assertThat(updated.targetIndustries()).containsExactly("Technology");
        assertThat(updated.targetRoles()).containsExactly("VP Sales");

        assertThat(icpDataService.findAllICPs())
            .extracting(ICPDto::id)
            .contains(created.id());

        assertThat(icpDataService.deleteICP(created.id())).isTrue();
        assertThat(icpDataService.findICP(created.id())).isNull();
    }
}
