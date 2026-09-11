package com.lagermanagement.space.domain.repository;

import com.lagermanagement.space.domain.entity.Lieferant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LieferantRepository extends JpaRepository<Lieferant, Long> {

    Optional<Lieferant> findByLieferantId(String lieferantId);

    boolean existsByLieferantId(String lieferantId);
}
