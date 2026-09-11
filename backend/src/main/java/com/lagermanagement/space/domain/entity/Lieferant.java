package com.lagermanagement.space.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "lieferanten")
@EntityListeners(AuditingEntityListener.class)
public class Lieferant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String lieferantId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 200)
    private String kontaktEmail;

    @Column(length = 50)
    private String kontaktTelefon;

    @Builder.Default
    @Column(nullable = false)
    private int leadTimeTage = 1;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime erstelltAm;
}
