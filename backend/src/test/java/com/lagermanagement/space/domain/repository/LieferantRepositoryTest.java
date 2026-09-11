package com.lagermanagement.space.domain.repository;

import com.lagermanagement.space.config.JpaAuditingConfig;
import com.lagermanagement.space.domain.entity.Lieferant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Import(JpaAuditingConfig.class)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.sql.init.mode=always",
        "app.security.operator-password-hash=test-hash-fuer-tests"
})
class LieferantRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private LieferantRepository lieferantRepository;

    @BeforeEach
    void setUp() {
        lieferantRepository.deleteAll();
    }

    @Test
    void shouldSaveAndFindLieferantByLieferantId() {
        Lieferant lieferant = Lieferant.builder()
                .lieferantId("LF-001")
                .name("Müller GmbH")
                .kontaktEmail("kontakt@mueller.de")
                .kontaktTelefon("+49 30 12345")
                .leadTimeTage(5)
                .build();
        lieferantRepository.save(lieferant);

        Optional<Lieferant> found = lieferantRepository.findByLieferantId("LF-001");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Müller GmbH");
        assertThat(found.get().getLeadTimeTage()).isEqualTo(5);
        assertThat(found.get().getErstelltAm()).isNotNull();
    }

    @Test
    void shouldReturnEmptyWhenLieferantIdNotFound() {
        assertThat(lieferantRepository.findByLieferantId("LF-NICHT-VORHANDEN")).isEmpty();
    }

    @Test
    void shouldDetectExistingLieferantId() {
        lieferantRepository.save(Lieferant.builder()
                .lieferantId("LF-EXISTS")
                .name("Schmidt AG")
                .leadTimeTage(3)
                .build());

        assertThat(lieferantRepository.existsByLieferantId("LF-EXISTS")).isTrue();
        assertThat(lieferantRepository.existsByLieferantId("LF-NOT-EXISTS")).isFalse();
    }

    @Test
    void shouldUseDefaultLeadTimeTageWhenNotSet() {
        Lieferant lieferant = Lieferant.builder()
                .lieferantId("LF-DEFAULT")
                .name("Default Lieferant")
                .build();
        Lieferant saved = lieferantRepository.save(lieferant);

        assertThat(saved.getLeadTimeTage()).isEqualTo(1);
    }

    @Test
    void shouldPopulateErstelltAmViaAuditing() {
        Lieferant lieferant = Lieferant.builder()
                .lieferantId("LF-AUDIT")
                .name("Audit Test")
                .leadTimeTage(2)
                .build();
        Lieferant saved = lieferantRepository.save(lieferant);

        assertThat(saved.getErstelltAm()).isNotNull();
    }
}
