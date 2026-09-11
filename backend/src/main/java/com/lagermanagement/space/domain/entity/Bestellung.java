package com.lagermanagement.space.domain.entity;

import com.lagermanagement.space.domain.enums.BestellungStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bestellungen")
@EntityListeners(AuditingEntityListener.class)
public class Bestellung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Format: BEST-{YYYY}-{NNN}
    @Column(unique = true, nullable = false, length = 50)
    private String bestellnummer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artikel_id", nullable = false)
    private Artikel artikel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lieferant_id", nullable = false)
    private Lieferant lieferant;

    @Column(nullable = false)
    private int bestellmenge;

    @Column(precision = 10, scale = 2)
    private BigDecimal einkaufspreis;

    private LocalDate gewuenschtesLieferdatum;

    @Column(columnDefinition = "TEXT")
    private String notiz;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BestellungStatus status = BestellungStatus.OFFEN;

    // Wird durch OrderService via SecurityContextHolder gesetzt
    @Column(nullable = false, length = 100)
    private String erstelltVon;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime erstelltAm;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime aktualisiertAm;
}
