package com.lagermanagement.space.domain.repository;

import com.lagermanagement.space.config.JpaAuditingConfig;
import com.lagermanagement.space.domain.entity.Artikel;
import com.lagermanagement.space.domain.entity.Lieferant;
import com.lagermanagement.space.domain.enums.ArtikelStatus;
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
class ArtikelRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private ArtikelRepository artikelRepository;

    @Autowired
    private LieferantRepository lieferantRepository;

    private Lieferant testLieferant;

    @BeforeEach
    void setUp() {
        artikelRepository.deleteAll();
        lieferantRepository.deleteAll();
        testLieferant = lieferantRepository.save(
                Lieferant.builder()
                        .lieferantId("LF-TEST-001")
                        .name("Test Lieferant GmbH")
                        .leadTimeTage(7)
                        .build());
    }

    @Test
    void shouldSaveAndFindArtikelByArtikelnummer() {
        Artikel artikel = Artikel.builder()
                .artikelnummer("ART-001")
                .bezeichnung("Testprodukt")
                .mengeneinheit("Stk")
                .warengruppe("Elektronik")
                .lieferant(testLieferant)
                .build();
        artikelRepository.save(artikel);

        Optional<Artikel> found = artikelRepository.findByArtikelnummer("ART-001");

        assertThat(found).isPresent();
        assertThat(found.get().getBezeichnung()).isEqualTo("Testprodukt");
        assertThat(found.get().getStatus()).isEqualTo(ArtikelStatus.AKTIV);
        assertThat(found.get().getErstelltAm()).isNotNull();
    }

    @Test
    void shouldReturnEmptyWhenArtikelNotFound() {
        assertThat(artikelRepository.findByArtikelnummer("NICHT-VORHANDEN")).isEmpty();
    }

    @Test
    void shouldFindOnlyActiveArtikel() {
        artikelRepository.save(Artikel.builder()
                .artikelnummer("ART-AKTIV").bezeichnung("Aktiv").mengeneinheit("Stk").warengruppe("A").build());
        artikelRepository.save(Artikel.builder()
                .artikelnummer("ART-INAKTIV").bezeichnung("Inaktiv").mengeneinheit("Stk").warengruppe("A")
                .status(ArtikelStatus.INAKTIV).build());

        List<Artikel> aktiv = artikelRepository.findAllByStatus(ArtikelStatus.AKTIV);

        assertThat(aktiv).hasSize(1);
        assertThat(aktiv.get(0).getArtikelnummer()).isEqualTo("ART-AKTIV");
    }

    @Test
    void shouldDetectExistingArtikelnummer() {
        artikelRepository.save(Artikel.builder()
                .artikelnummer("ART-EXISTS").bezeichnung("Vorhanden").mengeneinheit("Stk").warengruppe("B").build());

        assertThat(artikelRepository.existsByArtikelnummer("ART-EXISTS")).isTrue();
        assertThat(artikelRepository.existsByArtikelnummer("ART-NOT-EXISTS")).isFalse();
    }

    @Test
    void shouldFindArtikelAboveBestellpunkt() {
        // Bestellpunkt 10 → wird bei Reorder-Analyse berücksichtigt
        artikelRepository.save(Artikel.builder()
                .artikelnummer("ART-REORDER").bezeichnung("Nachbestellen").mengeneinheit("Kg").warengruppe("B")
                .bestellpunkt(10).build());
        // Bestellpunkt 0 → kein Reorder-Bedarf
        artikelRepository.save(Artikel.builder()
                .artikelnummer("ART-OK").bezeichnung("Ausreichend").mengeneinheit("Kg").warengruppe("B")
                .bestellpunkt(0).build());

        List<Artikel> reorder = artikelRepository.findAllByStatusAndBestellpunktGreaterThan(ArtikelStatus.AKTIV, 0);

        assertThat(reorder).hasSize(1);
        assertThat(reorder.get(0).getArtikelnummer()).isEqualTo("ART-REORDER");
    }
}
