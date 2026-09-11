package com.lagermanagement.space.domain.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DomainEnumsTest {

    @Test
    void shouldContainAllArtikelStatusValues() {
        assertThat(ArtikelStatus.values())
                .containsExactlyInAnyOrder(ArtikelStatus.AKTIV, ArtikelStatus.INAKTIV);
    }

    @Test
    void shouldContainAllVorschlagStatusValues() {
        assertThat(VorschlagStatus.values())
                .containsExactlyInAnyOrder(
                        VorschlagStatus.VORSCHLAG,
                        VorschlagStatus.BESTELLT,
                        VorschlagStatus.GELIEFERT,
                        VorschlagStatus.IGNORIERT);
    }

    @Test
    void shouldContainAllTransaktionTypValues() {
        assertThat(TransaktionTyp.values())
                .containsExactlyInAnyOrder(TransaktionTyp.EINGANG, TransaktionTyp.AUSGANG);
    }

    @Test
    void shouldContainAllTransaktionQuelleValues() {
        assertThat(TransaktionQuelle.values())
                .containsExactlyInAnyOrder(TransaktionQuelle.BATCH, TransaktionQuelle.MANUELL);
    }

    @Test
    void shouldMatchSchemaDefaultForArtikelStatus() {
        // schema.sql DEFAULT 'AKTIV'
        assertThat(ArtikelStatus.AKTIV.name()).isEqualTo("AKTIV");
    }

    @Test
    void shouldMatchSchemaDefaultForVorschlagStatus() {
        // schema.sql DEFAULT 'VORSCHLAG'
        assertThat(VorschlagStatus.VORSCHLAG.name()).isEqualTo("VORSCHLAG");
    }

    @Test
    void shouldMatchSchemaDefaultForTransaktionQuelle() {
        // schema.sql DEFAULT 'BATCH'
        assertThat(TransaktionQuelle.BATCH.name()).isEqualTo("BATCH");
    }
}
