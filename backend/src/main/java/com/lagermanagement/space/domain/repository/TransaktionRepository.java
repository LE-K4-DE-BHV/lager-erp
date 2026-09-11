package com.lagermanagement.space.domain.repository;

import com.lagermanagement.space.domain.entity.Transaktion;
import com.lagermanagement.space.domain.enums.TransaktionTyp;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TransaktionRepository extends JpaRepository<Transaktion, Long> {

    List<Transaktion> findByArtikelIdAndTypAndDatumBetween(
            Long artikelId, TransaktionTyp typ, LocalDate von, LocalDate bis);

    List<Transaktion> findAllByArtikelIdOrderByDatumDesc(Long artikelId);

    Page<Transaktion> findAllByDatumBetween(LocalDate von, LocalDate bis, Pageable pageable);

    Page<Transaktion> findAllByArtikelIdAndDatumBetween(Long artikelId, LocalDate von, LocalDate bis, Pageable pageable);

    Page<Transaktion> findAllByArtikelId(Long artikelId, Pageable pageable);
}
