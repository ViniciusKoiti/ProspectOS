package dev.prospectos;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "file:.env")
@ActiveProfiles("test")
class ProspectosApplicationTests {

    @Test
    void contextLoads() {
    }

}
