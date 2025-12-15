package com.sta.biometric.modelo;

import java.time.*;

import org.openxava.annotations.*;

import com.sta.biometric.calculadores.*;

import lombok.*;

/**
 * Clase auxiliar para el diálogo de selección de período entre fechas.
 * 
 * <p>
 * Permite al usuario especificar el rango de fechas por ejemplo para generar
 * una liquidación de jornadas.
 * </p>
 * 
 * @author Sistema STARH
 * @since 2.0
 */

@Getter
@Setter
@View(members = "periodoDesde; periodoHasta")
public class RangoFechas {

    /**
     * Fecha de inicio del período.
     * Por defecto: primer día del mes actual.
     */
    @Required
    @Stereotype("FECHA")
    @DefaultValueCalculator(InicioMesActualCalculator.class)
    private LocalDate periodoDesde;

    /**
     * Fecha de fin del período.
     * Por defecto: último día del mes actual.
     */
    @Required
    @Stereotype("FECHA")
    @DefaultValueCalculator(FinMesActualCalculator.class)
    private LocalDate periodoHasta;
}
