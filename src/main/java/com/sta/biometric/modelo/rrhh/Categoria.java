package com.sta.biometric.modelo.rrhh;

import java.math.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.model.*;

import com.sta.biometric.anotaciones.*;

import lombok.*;

/**
 * Categoría profesional dentro de un Convenio.
 * Define la escala salarial (Básico).
 */
@Entity
@Getter
@Setter
@View(members = "codigo, nombre; Valores [ basicoMensual, valorHora ]")
public class Categoria extends Identifiable {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ReferenceView("simple")
    private ConvenioColectivo convenio;

    @Column(length = 10)
    @Required
    private String codigo; // Ej: "A", "OFICIAL"

    @Column(length = 50)
    @Required
    @Capitalizar
    private String nombre; // Ej: "Administrativo A"

    // --- Escala Salarial Vigente ---
    // (En una versión avanzada esto debería ser un histórico,
    // pero para Fase 1 usamos valor actual)

    @Money
    private BigDecimal basicoMensual; // Para mensualizados

    @Money
    private BigDecimal valorHora; // Para jornalizados

}
