package com.lagermanagement.space.domain.repository;

import com.lagermanagement.space.domain.entity.Bestellung;
import com.lagermanagement.space.domain.enums.BestellungStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BestellungRepository extends JpaRepository<Bestellung, Long> {

    Optional<Bestellung> findByBestellnummer(String bestellnummer);

    List<Bestellung> findAllByArtikelIdAndStatus(Long artikelId, BestellungStatus status);

    long countByErstelltAmBetween(LocalDateTime von, LocalDateTime bis);
}
