package com.lagermanagement.space.domain.repository;

import com.lagermanagement.space.config.JpaAuditingConfig;
import com.lagermanagement.space.domain.entity.Artikel;
import com.lagermanagement.space.domain.entity.Bestellung;
import com.lagermanagement.space.domain.entity.Lieferant;
import com.lagermanagement.space.domain.enums.BestellungStatus;
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

import java.time.LocalDateTime;
import java.util.List;
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
class BestellungRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private BestellungRepository bestellungRepository;

    @Autowired
    private ArtikelRepository artikelRepository;

    @Autowired
    private LieferantRepository lieferantRepository;

    private Artikel testArtikel;
    private Lieferant testLieferant;

    @BeforeEach
    void setUp() {
        bestellungRepository.deleteAll();
        artikelRepository.deleteAll();
        lieferantRepository.deleteAll();

        testLieferant = lieferantRepository.save(Lieferant.builder()
                .lieferantId("LF-TEST")
                .name("Test Lieferant GmbH")
                .leadTimeTage(7)
                .build());

        testArtikel = artikelRepository.save(Artikel.builder()
                .artikelnummer("ART-BEST-001")
                .bezeichnung("Bestelltest Artikel")
                .mengeneinheit("Stk")
                .warengruppe("Test")
                .lieferant(testLieferant)
                .build());
    }

    @Test
    void shouldSaveAndFindBestellungByBestellnummer() {
        Bestellung bestellung = Bestellung.builder()
                .bestellnummer("BEST-2026-001")
                .artikel(testArtikel)
                .lieferant(testLieferant)
                .bestellmenge(50)
                .erstelltVon("operator")
                .build();
        bestellungRepository.save(bestellung);

        Optional<Bestellung> found = bestellungRepository.findByBestellnummer("BEST-2026-001");

        assertThat(found).isPresent();
        assertThat(found.get().getBestellmenge()).isEqualTo(50);
        assertThat(found.get().getStatus()).isEqualTo(BestellungStatus.OFFEN);
        assertThat(found.get().getErstelltVon()).isEqualTo("operator");
        assertThat(found.get().getErstelltAm()).isNotNull();
    }

    @Test
    void shouldReturnEmptyWhenBestellnummerNotFound() {
        assertThat(bestellungRepository.findByBestellnummer("BEST-NICHT-VORHANDEN")).isEmpty();
    }

    @Test
    void shouldFindBestellungenByArtikelIdAndStatus() {
        bestellungRepository.save(Bestellung.builder()
                .bestellnummer("BEST-2026-002")
                .artikel(testArtikel)
                .lieferant(testLieferant)
                .bestellmenge(20)
                .status(BestellungStatus.OFFEN)
                .erstelltVon("operator")
                .build());
        bestellungRepository.save(Bestellung.builder()
                .bestellnummer("BEST-2026-003")
                .artikel(testArtikel)
                .lieferant(testLieferant)
                .bestellmenge(30)
                .status(BestellungStatus.GELIEFERT)
                .erstelltVon("operator")
                .build());

        List<Bestellung> offene = bestellungRepository.findAllByArtikelIdAndStatus(
                testArtikel.getId(), BestellungStatus.OFFEN);

        assertThat(offene).hasSize(1);
        assertThat(offene.get(0).getBestellnummer()).isEqualTo("BEST-2026-002");
    }

    @Test
    void shouldCountBestellungenBetweenDates() {
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);

        bestellungRepository.save(Bestellung.builder()
                .bestellnummer("BEST-COUNT-001")
                .artikel(testArtikel)
                .lieferant(testLieferant)
                .bestellmenge(10)
                .erstelltVon("operator")
                .build());
        bestellungRepository.save(Bestellung.builder()
                .bestellnummer("BEST-COUNT-002")
                .artikel(testArtikel)
                .lieferant(testLieferant)
                .bestellmenge(10)
                .erstelltVon("operator")
                .build());

        LocalDateTime after = LocalDateTime.now().plusSeconds(1);

        long count = bestellungRepository.countByErstelltAmBetween(before, after);

        assertThat(count).isEqualTo(2);
    }

    @Test
    void shouldHaveDefaultStatusOffen() {
        Bestellung bestellung = Bestellung.builder()
                .bestellnummer("BEST-DEFAULT-STATUS")
                .artikel(testArtikel)
                .lieferant(testLieferant)
                .bestellmenge(5)
                .erstelltVon("operator")
                .build();
        Bestellung saved = bestellungRepository.save(bestellung);

        assertThat(saved.getStatus()).isEqualTo(BestellungStatus.OFFEN);
    }
}
