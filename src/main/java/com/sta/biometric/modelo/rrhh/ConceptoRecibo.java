package com.sta.biometric.modelo.rrhh;

import java.math.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.model.*;

import com.sta.biometric.anotaciones.*;
import com.sta.biometric.modelo.rrhh.enums.*;

import lombok.*;

/**
 * Concepto de liquidación (Item del recibo).
 * Ej: 100 - Básico, 201 - Jubilación, 500 - Retención Ganancias.
 */
@Entity
@Getter
@Setter
@View(members = "Identificacion [ codigo, descripcion; tipo ];" +
        "Configuracion [ porcentaje; montoFijo; ordenCalculo ];" +
        "Logica [ scriptCalculo; esProporcionalAInasistencias; esVariableNovedad ]")
public class ConceptoRecibo extends Identifiable {

    @Column(length = 10, unique = true)
    @SearchKey
    @Required
    private String codigo; // Ej: "100"

    @Column(length = 100)
    @Required
    @Capitalizar
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Required
    private TipoConcepto tipo;

    // --- Valores por defecto (si aplica) ---

    @Column(scale = 2)
    private BigDecimal porcentaje; // Ej: 11.00 para Jubilación

    @Money
    private BigDecimal montoFijo;

    // --- Lógica del Motor ---

    @Column(length = 50)
    private String scriptCalculo; // Nombre del Bean/Class que ejecuta el cálculo (Strategy Pattern)

    private int ordenCalculo; // Para asegurar que "Bruto" esté antes que "Deducciones"

    private boolean esProporcionalAInasistencias; // Si se descuenta por faltas

    private boolean esVariableNovedad; // Si depende de una novedad cargada (ej. horas extras)

}
