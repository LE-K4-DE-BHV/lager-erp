package com.lagermanagement.space.domain.repository;

import com.lagermanagement.space.config.JpaAuditingConfig;
import com.lagermanagement.space.domain.entity.ImportLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Import(JpaAuditingConfig.class)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.sql.init.mode=always",
        "app.security.operator-password-hash=test-hash-fuer-tests"
})
class ImportLogRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private ImportLogRepository importLogRepository;

    @BeforeEach
    void setUp() {
        importLogRepository.deleteAll();
    }

    @Test
    void shouldSaveImportLogAndAssignId() {
        ImportLog log = ImportLog.builder()
                .dateiname("stammdaten_2024.csv")
                .dateiHash("a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2")
                .typ("STAMMDATEN_ARTIKEL")
                .status("SUCCESS")
                .zeilenGesamt(100)
                .zeilenErfolgreich(98)
                .zeilenFehlerhaft(2)
                .build();

        ImportLog saved = importLogRepository.save(log);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getId()).isPositive();
    }

    @Test
    void shouldAutoFillVerarbeitetAmViaCreditDate() {
        ImportLog log = ImportLog.builder()
                .dateiname("transaktionen.csv")
                .dateiHash("b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3")
                .typ("TRANSAKTIONEN_EINGANG")
                .status("SUCCESS")
                .build();

        ImportLog saved = importLogRepository.saveAndFlush(log);

        assertThat(saved.getVerarbeitetAm()).isNotNull();
    }

    @Test
    void shouldReturnTrueWhenDateiHashExists() {
        String hash = "c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4";
        importLogRepository.save(ImportLog.builder()
                .dateiname("lieferanten.csv")
                .dateiHash(hash)
                .typ("STAMMDATEN_LIEFERANTEN")
                .status("SUCCESS")
                .build());

        assertThat(importLogRepository.existsByDateiHash(hash)).isTrue();
    }

    @Test
    void shouldReturnFalseWhenDateiHashNotExists() {
        String unbekannterHash = "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";

        assertThat(importLogRepository.existsByDateiHash(unbekannterHash)).isFalse();
    }

    @Test
    void shouldEnforceUniqueConstraintOnDateiHash() {
        String duplikatHash = "d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5";

        importLogRepository.saveAndFlush(ImportLog.builder()
                .dateiname("datei1.csv")
                .dateiHash(duplikatHash)
                .typ("STAMMDATEN_ARTIKEL")
                .status("SUCCESS")
                .build());

        assertThatThrownBy(() ->
                importLogRepository.saveAndFlush(ImportLog.builder()
                        .dateiname("datei2.csv")
                        .dateiHash(duplikatHash)
                        .typ("STAMMDATEN_ARTIKEL")
                        .status("SUCCESS")
                        .build())
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldSaveImportLogWithFehlerDetailsWhenStatusIsError() {
        ImportLog log = ImportLog.builder()
                .dateiname("fehlerhaft.csv")
                .dateiHash("e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6")
                .typ("TRANSAKTIONEN_AUSGANG")
                .status("ERROR")
                .zeilenGesamt(50)
                .zeilenErfolgreich(0)
                .zeilenFehlerhaft(50)
                .fehlerDetails("Zeile 1: Ungültige Artikelnummer. Zeile 2: Datum fehlt.")
                .build();

        ImportLog saved = importLogRepository.save(log);

        assertThat(saved.getFehlerDetails()).contains("Ungültige Artikelnummer");
        assertThat(saved.getStatus()).isEqualTo("ERROR");
    }

    @Test
    void shouldPreventDoppelverarbeitungByHashCheck() {
        String hash = "f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1";

        importLogRepository.save(ImportLog.builder()
                .dateiname("erste-verarbeitung.csv")
                .dateiHash(hash)
                .typ("STAMMDATEN_ARTIKEL")
                .status("SUCCESS")
                .build());

        // Idempotenz-Check: Hash bereits verarbeitet → keine erneute Verarbeitung
        boolean bereitsVerarbeitet = importLogRepository.existsByDateiHash(hash);

        assertThat(bereitsVerarbeitet).isTrue();
    }
}
