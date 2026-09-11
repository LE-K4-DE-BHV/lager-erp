package com.lagermanagement.space.domain.entity;

import com.lagermanagement.space.domain.enums.ArtikelStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "artikel")
@EntityListeners(AuditingEntityListener.class)
public class Artikel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String artikelnummer;

    @Column(nullable = false, length = 200)
    private String bezeichnung;

    @Column(nullable = false, length = 20)
    private String mengeneinheit;

    @Column(nullable = false, length = 100)
    private String warengruppe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lieferant_id")
    private Lieferant lieferant;

    @Builder.Default
    @Column(nullable = false)
    private int aktuellerBestand = 0;

    @Builder.Default
    @Column(nullable = false)
    private int sicherheitsbestand = 0;

    @Builder.Default
    @Column(nullable = false)
    private int bestellpunkt = 0;

    @Builder.Default
    @Column(nullable = false)
    private int standardBestellmenge = 1;

    @Column(precision = 10, scale = 2)
    private BigDecimal einkaufspreis;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ArtikelStatus status = ArtikelStatus.AKTIV;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime erstelltAm;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime aktualisiertAm;
}
