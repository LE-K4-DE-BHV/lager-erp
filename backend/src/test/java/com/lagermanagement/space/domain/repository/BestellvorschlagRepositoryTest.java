package com.lagermanagement.space.domain.repository;

import com.lagermanagement.space.config.JpaAuditingConfig;
import com.lagermanagement.space.domain.entity.Artikel;
import com.lagermanagement.space.domain.entity.Bestellvorschlag;
import com.lagermanagement.space.domain.entity.Lieferant;
import com.lagermanagement.space.domain.enums.VorschlagStatus;
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
class BestellvorschlagRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private BestellvorschlagRepository bestellvorschlagRepository;

    @Autowired
    private ArtikelRepository artikelRepository;

    @Autowired
    private LieferantRepository lieferantRepository;

    private Artikel testArtikel;
    private Artikel andererArtikel;

    @BeforeEach
    void setUp() {
        bestellvorschlagRepository.deleteAll();
        artikelRepository.deleteAll();
        lieferantRepository.deleteAll();

        Lieferant lieferant = lieferantRepository.save(Lieferant.builder()
                .lieferantId("LF-VS-TEST")
                .name("Vorschlag Lieferant")
                .leadTimeTage(5)
                .build());

        testArtikel = artikelRepository.save(Artikel.builder()
                .artikelnummer("ART-VS-001")
                .bezeichnung("Vorschlag Artikel 1")
                .mengeneinheit("Stk")
                .warengruppe("Test")
                .lieferant(lieferant)
                .build());

        andererArtikel = artikelRepository.save(Artikel.builder()
                .artikelnummer("ART-VS-002")
                .bezeichnung("Vorschlag Artikel 2")
                .mengeneinheit("Kg")
                .warengruppe("Test")
                .build());
    }

    @Test
    void shouldHaveDefaultStatusVorschlagWhenSaved() {
        Bestellvorschlag vorschlag = bestellvorschlagRepository.save(Bestellvorschlag.builder()
                .artikel(testArtikel)
                .bestandBeiErstellung(5)
                .vorgeschlageneMenge(20)
                .build());

        assertThat(vorschlag.getStatus()).isEqualTo(VorschlagStatus.VORSCHLAG);
        assertThat(vorschlag.getErstelltAm()).isNotNull();
    }

    @Test
    void shouldFindVorschlaegeByArtikelIdAndStatusIn() {
        bestellvorschlagRepository.save(Bestellvorschlag.builder()
                .artikel(testArtikel)
                .bestandBeiErstellung(5)
                .vorgeschlageneMenge(20)
                .status(VorschlagStatus.VORSCHLAG)
                .build());
        bestellvorschlagRepository.save(Bestellvorschlag.builder()
                .artikel(testArtikel)
                .bestandBeiErstellung(3)
                .vorgeschlageneMenge(15)
                .status(VorschlagStatus.BESTELLT)
                .build());
        bestellvorschlagRepository.save(Bestellvorschlag.builder()
                .artikel(andererArtikel)
                .bestandBeiErstellung(2)
                .vorgeschlageneMenge(10)
                .status(VorschlagStatus.VORSCHLAG)
                .build());

        // Duplikat-Prüfung: offene Vorschläge (VORSCHLAG + BESTELLT) für testArtikel
        List<Bestellvorschlag> aktive = bestellvorschlagRepository.findByArtikelIdAndStatusIn(
                testArtikel.getId(), List.of(VorschlagStatus.VORSCHLAG, VorschlagStatus.BESTELLT));

        assertThat(aktive).hasSize(2);
        assertThat(aktive).allMatch(v -> v.getArtikel().getId().equals(testArtikel.getId()));
    }

    @Test
    void shouldFindAllVorschlaegeByStatusIn() {
        bestellvorschlagRepository.save(Bestellvorschlag.builder()
                .artikel(testArtikel)
                .bestandBeiErstellung(5)
                .vorgeschlageneMenge(20)
                .status(VorschlagStatus.VORSCHLAG)
                .build());
        bestellvorschlagRepository.save(Bestellvorschlag.builder()
                .artikel(andererArtikel)
                .bestandBeiErstellung(2)
                .vorgeschlageneMenge(10)
                .status(VorschlagStatus.IGNORIERT)
                .build());
        bestellvorschlagRepository.save(Bestellvorschlag.builder()
                .artikel(testArtikel)
                .bestandBeiErstellung(0)
                .vorgeschlageneMenge(30)
                .status(VorschlagStatus.GELIEFERT)
                .build());

        List<Bestellvorschlag> offene = bestellvorschlagRepository.findAllByStatusIn(
                List.of(VorschlagStatus.VORSCHLAG));

        assertThat(offene).hasSize(1);
        assertThat(offene.get(0).getStatus()).isEqualTo(VorschlagStatus.VORSCHLAG);
    }

    @Test
    void shouldFindTopVorschlagByArtikelIdAndStatusOrderByErstelltAmDesc() {
        // Zwei VORSCHLAG-Einträge für denselben Artikel — der neueste soll zurückgegeben werden
        Bestellvorschlag erster = bestellvorschlagRepository.save(Bestellvorschlag.builder()
                .artikel(testArtikel)
                .bestandBeiErstellung(10)
                .vorgeschlageneMenge(20)
                .status(VorschlagStatus.VORSCHLAG)
                .build());
        Bestellvorschlag zweiter = bestellvorschlagRepository.save(Bestellvorschlag.builder()
                .artikel(testArtikel)
                .bestandBeiErstellung(5)
                .vorgeschlageneMenge(25)
                .status(VorschlagStatus.VORSCHLAG)
                .build());

        Optional<Bestellvorschlag> latest = bestellvorschlagRepository
                .findTopByArtikelIdAndStatusOrderByErstelltAmDesc(testArtikel.getId(), VorschlagStatus.VORSCHLAG);

        assertThat(latest).isPresent();
        // Der zuletzt gespeicherte hat die höhere ID
        assertThat(latest.get().getId()).isGreaterThanOrEqualTo(zweiter.getId());
        assertThat(latest.get().getVorgeschlageneMenge()).isEqualTo(25);
    }

    @Test
    void shouldReturnEmptyWhenNoMatchingVorschlagExists() {
        Optional<Bestellvorschlag> result = bestellvorschlagRepository
                .findTopByArtikelIdAndStatusOrderByErstelltAmDesc(testArtikel.getId(), VorschlagStatus.BESTELLT);

        assertThat(result).isEmpty();
    }
}
