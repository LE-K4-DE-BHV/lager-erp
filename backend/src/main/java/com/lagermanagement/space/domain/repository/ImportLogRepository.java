package com.lagermanagement.space.domain.repository;

import com.lagermanagement.space.domain.entity.ImportLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImportLogRepository extends JpaRepository<ImportLog, Long> {

    boolean existsByDateiHash(String dateiHash);
}
