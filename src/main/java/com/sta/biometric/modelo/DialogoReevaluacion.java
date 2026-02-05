package com.sta.biometric.modelo;

import java.time.*;

import org.openxava.calculators.*;
import org.openxava.annotations.*;
import lombok.*;

/**
 * Modelo transitorio para el diálogo de reevaluación de jornadas.
 */
@Getter
@Setter
public class DialogoReevaluacion {

    @Required
    @DefaultValueCalculator(CurrentDateCalculator.class)
    private LocalDate fechaDesde;

    @Required
    @DefaultValueCalculator(CurrentDateCalculator.class)
    private LocalDate fechaHasta;

}
