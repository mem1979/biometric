package com.sta.biometric.modelo;

import java.time.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.calculators.*;

import com.sta.biometric.enums.*;

import lombok.*;

/**
 * Nota de desempeño para evaluar al personal.
 * Permite registrar observaciones con una calificación (Buena, Normal, Mala)
 * para posteriormente calcular métricas de desempeño.
 */
@Entity
@Table(name = "NotasDesempeno")
@Getter
@Setter
public class NotaDesempeno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Hidden
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_id", nullable = false)
    @ReferenceView("Simple")
    @ReadOnly
    private Personal empleado;

    @Column(length = 1000, nullable = false)
    @Required
    @Stereotype("MEMO")
    private String contenido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Required
    private CalificacionNota calificacion;

    @Column(nullable = false)
    @ReadOnly
    private LocalDateTime fechaHora;

    @Column(length = 50)
    @ReadOnly
    @DefaultValueCalculator(CurrentUserCalculator.class)
    private String autor;

    @PrePersist
    protected void onCreate() {
        if (fechaHora == null) {
            fechaHora = LocalDateTime.now();
        }
    }
}
