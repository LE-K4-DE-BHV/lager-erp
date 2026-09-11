package com.lagermanagement.space.domain.repository;

import com.lagermanagement.space.domain.entity.Bestellvorschlag;
import com.lagermanagement.space.domain.enums.VorschlagStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BestellvorschlagRepository extends JpaRepository<Bestellvorschlag, Long> {

    List<Bestellvorschlag> findByArtikelIdAndStatusIn(Long artikelId, List<VorschlagStatus> statuses);

    List<Bestellvorschlag> findAllByStatusIn(List<VorschlagStatus> statuses);

    Optional<Bestellvorschlag> findTopByArtikelIdAndStatusOrderByErstelltAmDesc(Long artikelId, VorschlagStatus status);

    List<Bestellvorschlag> findByBestellungId(Long bestellungId);
}
