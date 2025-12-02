package com.sta.biometric.modelo;

import java.time.*;
import org.openxava.annotations.*;
import org.openxava.calculators.CurrentDateCalculator;
import lombok.*;

/**
 * Clase transitoria para solicitar el rango de fechas para el cálculo de horas.
 * Se utiliza en la acción CalcularHorasAction.
 */
@Getter
@Setter
@View(members = "fechaDesde; fechaHasta")
public class RangoFechas {

    @Required
    @DefaultValueCalculator(CurrentDateCalculator.class)
    private LocalDate fechaDesde;

    @Required
    @DefaultValueCalculator(CurrentDateCalculator.class)
    private LocalDate fechaHasta;

}
