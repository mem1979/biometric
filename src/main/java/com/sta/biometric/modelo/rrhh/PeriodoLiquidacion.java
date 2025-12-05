package com.sta.biometric.modelo.rrhh;

import java.time.*;

import javax.persistence.*;
import javax.validation.constraints.*;

import org.openxava.annotations.*;
import org.openxava.calculators.*;
import org.openxava.model.*;

import com.sta.biometric.modelo.rrhh.enums.*;

import lombok.*;

/**
 * Define el período fiscal y administrativo de liquidación.
 * Ej: "Noviembre 2024 - Mensual", "1ra Quincena Diciembre 2024".
 */
@Entity
@Getter
@Setter
@View(members = "Descripcion [ anio, mes; numeroQuincena, modalidad; descripcion ];" +
        "Fechas [ fechaInicio, fechaFin; fechaPago ]")
@Tab(properties = "descripcion, anio, mes, modalidad, fechaPago")
public class PeriodoLiquidacion extends Identifiable {

    @DefaultValueCalculator(CurrentYearCalculator.class)
    private int anio;

    @DefaultValueCalculator(CurrentMonthCalculator.class)
    @Max(12)
    @Min(1)
    private int mes;

    @Enumerated(EnumType.STRING)
    @Required
    private ModalidadLiquidacion modalidad; // MENSUAL o QUINCENAL

    @Max(2)
    @Min(1)
    private int numeroQuincena; // 1 o 2 (Solo si modalidad es Quincenal)

    @Required
    @Column(length = 100)
    private String descripcion; // Calculardo o manual

    @Required
    private LocalDate fechaInicio; // Para buscar fichadas

    @Required
    private LocalDate fechaFin;

    @Required
    private LocalDate fechaPago; // Fecha en que se abona (importante para AFIP)

}
