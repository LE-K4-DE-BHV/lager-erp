package com.lagermanagement.space.domain.repository;

import com.lagermanagement.space.domain.entity.Artikel;
import com.lagermanagement.space.domain.enums.ArtikelStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ArtikelRepository extends JpaRepository<Artikel, Long> {

    List<Artikel> findAllByStatus(ArtikelStatus status);

    Optional<Artikel> findByArtikelnummer(String artikelnummer);

    boolean existsByArtikelnummer(String artikelnummer);

    List<Artikel> findAllByStatusAndBestellpunktGreaterThan(ArtikelStatus status, int bestellpunkt);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Artikel a WHERE a.id = :id")
    Optional<Artikel> findByIdWithLock(@Param("id") Long id);
}
