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
@Table(name = "import_log")
@EntityListeners(AuditingEntityListener.class)
public class ImportLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String dateiname;

    @Column(name = "datei_hash", unique = true, nullable = false, length = 64)
    private String dateiHash;

    @Column(nullable = false, length = 50)
    private String typ;

    @Column(nullable = false, length = 20)
    private String status;

    private Integer zeilenGesamt;

    private Integer zeilenErfolgreich;

    private Integer zeilenFehlerhaft;

    @Column(columnDefinition = "TEXT")
    private String fehlerDetails;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime verarbeitetAm;
}
