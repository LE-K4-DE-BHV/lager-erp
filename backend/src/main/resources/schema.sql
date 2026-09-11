-- ============================================================
-- Lager-Management-System — Datenbankschema
-- Wird bei jedem Start ausgeführt (CREATE TABLE IF NOT EXISTS).
-- Keine Flyway/Liquibase-Migration — schema.sql ist die einzige Wahrheit.
-- ============================================================

CREATE TABLE IF NOT EXISTS lieferanten (
    id              BIGSERIAL PRIMARY KEY,
    lieferant_id    VARCHAR(50) UNIQUE NOT NULL,
    name            VARCHAR(200) NOT NULL,
    kontakt_email   VARCHAR(200),
    kontakt_telefon VARCHAR(50),
    lead_time_tage  INTEGER NOT NULL DEFAULT 1,
    erstellt_am     TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS artikel (
    id                    BIGSERIAL PRIMARY KEY,
    artikelnummer         VARCHAR(50) UNIQUE NOT NULL,
    bezeichnung           VARCHAR(200) NOT NULL,
    mengeneinheit         VARCHAR(20) NOT NULL,
    warengruppe           VARCHAR(100) NOT NULL,
    lieferant_id          BIGINT REFERENCES lieferanten(id),
    aktueller_bestand     INTEGER NOT NULL DEFAULT 0,
    sicherheitsbestand    INTEGER NOT NULL DEFAULT 0,
    bestellpunkt          INTEGER NOT NULL DEFAULT 0,
    standard_bestellmenge INTEGER NOT NULL DEFAULT 1,
    einkaufspreis         NUMERIC(10,2),
    status                VARCHAR(20) NOT NULL DEFAULT 'AKTIV',
    erstellt_am           TIMESTAMP NOT NULL DEFAULT NOW(),
    aktualisiert_am       TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS bestellungen (
    id                      BIGSERIAL PRIMARY KEY,
    bestellnummer           VARCHAR(50) UNIQUE NOT NULL,
    artikel_id              BIGINT NOT NULL REFERENCES artikel(id),
    lieferant_id            BIGINT NOT NULL REFERENCES lieferanten(id),
    bestellmenge            INTEGER NOT NULL,
    einkaufspreis           NUMERIC(10,2),
    gewuenschtes_lieferdatum DATE,
    notiz                   TEXT,
    status                  VARCHAR(20) NOT NULL DEFAULT 'OFFEN',
    erstellt_von            VARCHAR(100) NOT NULL,
    erstellt_am             TIMESTAMP NOT NULL DEFAULT NOW(),
    aktualisiert_am         TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS bestellvorschlaege (
    id                      BIGSERIAL PRIMARY KEY,
    artikel_id              BIGINT NOT NULL REFERENCES artikel(id),
    lieferant_id            BIGINT REFERENCES lieferanten(id),
    bestellung_id           BIGINT REFERENCES bestellungen(id),
    bestand_bei_erstellung  INTEGER NOT NULL,
    vorgeschlagene_menge    INTEGER NOT NULL,
    status                  VARCHAR(20) NOT NULL DEFAULT 'VORSCHLAG',
    erstellt_am             TIMESTAMP NOT NULL DEFAULT NOW(),
    aktualisiert_am         TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS transaktionen (
    id            BIGSERIAL PRIMARY KEY,
    artikel_id    BIGINT NOT NULL REFERENCES artikel(id),
    typ           VARCHAR(20) NOT NULL,
    buchungstyp   VARCHAR(50),
    menge         INTEGER NOT NULL,
    datum         DATE NOT NULL,
    quelle        VARCHAR(20) NOT NULL DEFAULT 'BATCH',
    bestellung_id BIGINT REFERENCES bestellungen(id),
    lieferant_id  BIGINT REFERENCES lieferanten(id),
    grund         TEXT,
    benutzer_id   VARCHAR(100),
    erstellt_am   TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS import_log (
    id                 BIGSERIAL PRIMARY KEY,
    dateiname          VARCHAR(500) NOT NULL,
    datei_hash         VARCHAR(64) NOT NULL UNIQUE,
    typ                VARCHAR(50) NOT NULL,
    status             VARCHAR(20) NOT NULL,
    zeilen_gesamt      INTEGER,
    zeilen_erfolgreich INTEGER,
    zeilen_fehlerhaft  INTEGER,
    fehler_details     TEXT,
    verarbeitet_am     TIMESTAMP NOT NULL DEFAULT NOW()
);
