package com.sta.biometric.modelo;

import java.time.*;

import org.openxava.annotations.*;
import org.openxava.calculators.*;

import lombok.*;

/**
 * Modelo transitorio para el diálogo de selección de fecha del informe diario.
 */
@Getter
@Setter
@View(members = "fecha")
public class FechaInformeDiarioDialog {

    @Required
    @Stereotype("FECHA")
    @DefaultValueCalculator(CurrentLocalDateCalculator.class)
    private LocalDate fecha;
}
