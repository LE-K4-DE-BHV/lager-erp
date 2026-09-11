package com.lagermanagement.space.domain.entity;

import com.lagermanagement.space.domain.enums.VorschlagStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bestellvorschlaege")
@EntityListeners(AuditingEntityListener.class)
public class Bestellvorschlag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artikel_id", nullable = false)
    private Artikel artikel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lieferant_id")
    private Lieferant lieferant;

    // Wird gesetzt, sobald der Vorschlag den Status BESTELLT erhält
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bestellung_id")
    private Bestellung bestellung;

    @Column(nullable = false)
    private int bestandBeiErstellung;

    @Column(nullable = false)
    private int vorgeschlageneMenge;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VorschlagStatus status = VorschlagStatus.VORSCHLAG;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime erstelltAm;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime aktualisiertAm;
}
