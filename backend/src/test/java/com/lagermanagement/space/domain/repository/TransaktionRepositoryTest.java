package com.lagermanagement.space.domain.repository;

import com.lagermanagement.space.config.JpaAuditingConfig;
import com.lagermanagement.space.domain.entity.Artikel;
import com.lagermanagement.space.domain.entity.Lieferant;
import com.lagermanagement.space.domain.entity.Transaktion;
import com.lagermanagement.space.domain.enums.TransaktionQuelle;
import com.lagermanagement.space.domain.enums.TransaktionTyp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;

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
class TransaktionRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private TransaktionRepository transaktionRepository;

    @Autowired
    private ArtikelRepository artikelRepository;

    @Autowired
    private LieferantRepository lieferantRepository;

    private Artikel testArtikel;
    private Artikel andererArtikel;

    private static final LocalDate TAG_1 = LocalDate.of(2026, 4, 1);
    private static final LocalDate TAG_2 = LocalDate.of(2026, 4, 10);
    private static final LocalDate TAG_3 = LocalDate.of(2026, 4, 20);

    @BeforeEach
    void setUp() {
        transaktionRepository.deleteAll();
        artikelRepository.deleteAll();
        lieferantRepository.deleteAll();

        testArtikel = artikelRepository.save(Artikel.builder()
                .artikelnummer("ART-TX-001")
                .bezeichnung("Transaktion Artikel 1")
                .mengeneinheit("Stk")
                .warengruppe("Test")
                .build());

        andererArtikel = artikelRepository.save(Artikel.builder()
                .artikelnummer("ART-TX-002")
                .bezeichnung("Transaktion Artikel 2")
                .mengeneinheit("Kg")
                .warengruppe("Test")
                .build());
    }

    @Test
    void shouldSaveTransaktionWithDefaultQuelleAndPopulateErstelltAm() {
        Transaktion transaktion = transaktionRepository.save(Transaktion.builder()
                .artikel(testArtikel)
                .typ(TransaktionTyp.EINGANG)
                .menge(100)
                .datum(TAG_1)
                .build());

        assertThat(transaktion.getQuelle()).isEqualTo(TransaktionQuelle.BATCH);
        assertThat(transaktion.getErstelltAm()).isNotNull();
    }

    @Test
    void shouldFindTransaktionenByArtikelIdAndTypAndDatumBetween() {
        // EINGANG innerhalb des Zeitraums
        transaktionRepository.save(Transaktion.builder()
                .artikel(testArtikel).typ(TransaktionTyp.EINGANG).menge(100).datum(TAG_1).build());
        // AUSGANG innerhalb des Zeitraums
        transaktionRepository.save(Transaktion.builder()
                .artikel(testArtikel).typ(TransaktionTyp.AUSGANG).menge(10).datum(TAG_2).build());
        // EINGANG außerhalb des Zeitraums (zu spät)
        transaktionRepository.save(Transaktion.builder()
                .artikel(testArtikel).typ(TransaktionTyp.EINGANG).menge(50).datum(TAG_3).build());
        // EINGANG eines anderen Artikels
        transaktionRepository.save(Transaktion.builder()
                .artikel(andererArtikel).typ(TransaktionTyp.EINGANG).menge(200).datum(TAG_1).build());

        List<Transaktion> result = transaktionRepository.findByArtikelIdAndTypAndDatumBetween(
                testArtikel.getId(), TransaktionTyp.EINGANG, TAG_1, TAG_2);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMenge()).isEqualTo(100);
        assertThat(result.get(0).getTyp()).isEqualTo(TransaktionTyp.EINGANG);
    }

    @Test
    void shouldFindAllTransaktionenByArtikelIdOrderByDatumDesc() {
        transaktionRepository.save(Transaktion.builder()
                .artikel(testArtikel).typ(TransaktionTyp.EINGANG).menge(100).datum(TAG_1).build());
        transaktionRepository.save(Transaktion.builder()
                .artikel(testArtikel).typ(TransaktionTyp.AUSGANG).menge(15).datum(TAG_3).build());
        transaktionRepository.save(Transaktion.builder()
                .artikel(testArtikel).typ(TransaktionTyp.AUSGANG).menge(5).datum(TAG_2).build());
        // Andere Artikel-Transaktion soll NICHT im Ergebnis sein
        transaktionRepository.save(Transaktion.builder()
                .artikel(andererArtikel).typ(TransaktionTyp.EINGANG).menge(50).datum(TAG_2).build());

        List<Transaktion> result = transaktionRepository
                .findAllByArtikelIdOrderByDatumDesc(testArtikel.getId());

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getDatum()).isEqualTo(TAG_3);
        assertThat(result.get(1).getDatum()).isEqualTo(TAG_2);
        assertThat(result.get(2).getDatum()).isEqualTo(TAG_1);
    }

    @Test
    void shouldFindPaginatedTransaktionenByDatumBetween() {
        // 3 Transaktionen im Zeitraum, 1 außerhalb
        transaktionRepository.save(Transaktion.builder()
                .artikel(testArtikel).typ(TransaktionTyp.EINGANG).menge(100).datum(TAG_1).build());
        transaktionRepository.save(Transaktion.builder()
                .artikel(testArtikel).typ(TransaktionTyp.AUSGANG).menge(10).datum(TAG_2).build());
        transaktionRepository.save(Transaktion.builder()
                .artikel(andererArtikel).typ(TransaktionTyp.EINGANG).menge(50).datum(TAG_2).build());
        transaktionRepository.save(Transaktion.builder()
                .artikel(testArtikel).typ(TransaktionTyp.AUSGANG).menge(5)
                .datum(LocalDate.of(2026, 5, 1)).build()); // außerhalb

        Page<Transaktion> page = transaktionRepository.findAllByDatumBetween(
                TAG_1, TAG_3, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent()).hasSize(3);
    }

    @Test
    void shouldReturnEmptyListWhenNoTransaktionenInDateRange() {
        transaktionRepository.save(Transaktion.builder()
                .artikel(testArtikel).typ(TransaktionTyp.EINGANG).menge(100).datum(TAG_3).build());

        List<Transaktion> result = transaktionRepository.findByArtikelIdAndTypAndDatumBetween(
                testArtikel.getId(), TransaktionTyp.EINGANG, TAG_1, TAG_2);

        assertThat(result).isEmpty();
    }
}
