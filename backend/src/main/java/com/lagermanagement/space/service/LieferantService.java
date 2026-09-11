package com.lagermanagement.space.service;

import com.lagermanagement.space.domain.entity.Lieferant;
import com.lagermanagement.space.domain.repository.LieferantRepository;
import com.lagermanagement.space.exception.EntityNotFoundException;
import com.lagermanagement.space.web.dto.LieferantDto;
import com.lagermanagement.space.web.dto.LieferantUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LieferantService {

    private final LieferantRepository lieferantRepository;

    @Transactional(readOnly = true)
    public List<LieferantDto> findAll() {
        return lieferantRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public LieferantDto findById(Long id) {
        return toDto(ladeLieferantOrThrow(id));
    }

    @Transactional
    public LieferantDto update(Long id, LieferantUpdateRequest request) {
        Lieferant lieferant = ladeLieferantOrThrow(id);

        // lieferantId (externe Geschäfts-ID) bleibt unveränderlich
        lieferant.setName(request.name());
        lieferant.setKontaktEmail(request.kontaktEmail());
        lieferant.setKontaktTelefon(request.kontaktTelefon());
        lieferant.setLeadTimeTage(request.leadTimeTage());

        log.info("Lieferant aktualisiert: {} ({})", lieferant.getLieferantId(), id);
        return toDto(lieferantRepository.save(lieferant));
    }

    // --- Hilfsmethoden ---

    private Lieferant ladeLieferantOrThrow(Long id) {
        return lieferantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Lieferant mit ID " + id + " nicht gefunden."));
    }

    LieferantDto toDto(Lieferant lieferant) {
        return new LieferantDto(
                lieferant.getId(),
                lieferant.getLieferantId(),
                lieferant.getName(),
                lieferant.getKontaktEmail(),
                lieferant.getKontaktTelefon(),
                lieferant.getLeadTimeTage()
        );
    }
}
