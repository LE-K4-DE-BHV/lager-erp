package com.lagermanagement.space.domain.entity;

import com.lagermanagement.space.domain.enums.TransaktionQuelle;
import com.lagermanagement.space.domain.enums.TransaktionTyp;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transaktionen")
@EntityListeners(AuditingEntityListener.class)
public class Transaktion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artikel_id", nullable = false)
    private Artikel artikel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransaktionTyp typ;

    // WARENEINGANG | Verbrauch intern | Verkauf | Verlust/Schwund | Retoure
    @Column(length = 50)
    private String buchungstyp;

    @Column(nullable = false)
    private int menge;

    @Column(nullable = false)
    private LocalDate datum;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransaktionQuelle quelle = TransaktionQuelle.BATCH;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bestellung_id")
    private Bestellung bestellung;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lieferant_id")
    private Lieferant lieferant;

    @Column(columnDefinition = "TEXT")
    private String grund;

    // Audit Trail für manuelle Buchungen — aus SecurityContextHolder befüllt
    @Column(length = 100)
    private String benutzerId;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime erstelltAm;
}
