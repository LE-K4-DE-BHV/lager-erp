package com.lagermanagement.space.service;

import com.lagermanagement.space.domain.entity.Bestellvorschlag;
import com.lagermanagement.space.domain.enums.VorschlagStatus;
import com.lagermanagement.space.domain.repository.BestellvorschlagRepository;
import com.lagermanagement.space.web.dto.BestellvorschlagDto;
import com.lagermanagement.space.web.dto.KpiDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DashboardService {

    private final BestellvorschlagRepository bestellvorschlagRepository;
    private final ReorderAnalysisService reorderAnalysisService;

    public KpiDto getKpis() {
        List<Bestellvorschlag> offene = bestellvorschlagRepository
                .findAllByStatusIn(List.of(VorschlagStatus.VORSCHLAG, VorschlagStatus.BESTELLT));

        int kritisch = (int) offene.stream()
                .filter(v -> v.getArtikel().getAktuellerBestand() <= v.getArtikel().getSicherheitsbestand())
                .count();

        return new KpiDto(offene.size(), kritisch, reorderAnalysisService.getLetzterReorderLauf());
    }

    public List<BestellvorschlagDto> getVorschlaege() {
        return bestellvorschlagRepository
                .findAllByStatusIn(List.of(VorschlagStatus.VORSCHLAG, VorschlagStatus.BESTELLT))
                .stream()
                .map(this::toDto)
                .toList();
    }

    private BestellvorschlagDto toDto(Bestellvorschlag v) {
        var artikel = v.getArtikel();
        boolean kritisch = artikel.getAktuellerBestand() <= artikel.getSicherheitsbestand();
        String lieferantName = v.getLieferant() != null ? v.getLieferant().getName() : null;

        return new BestellvorschlagDto(
                v.getId(),
                artikel.getId(),
                artikel.getArtikelnummer(),
                artikel.getBezeichnung(),
                lieferantName,
                artikel.getAktuellerBestand(),
                artikel.getBestellpunkt(),
                artikel.getSicherheitsbestand(),
                v.getVorgeschlageneMenge(),
                v.getStatus(),
                v.getErstelltAm(),
                kritisch
        );
    }
}
