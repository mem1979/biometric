package com.sta.biometric.modelo;

import java.time.*;

import org.openxava.annotations.*;
import org.openxava.calculators.*;

import lombok.*;

/**
 * Modelo transitorio para el diálogo de selección de período de liquidación.
 * 
 * <p>
 * Permite al usuario especificar el rango de fechas para generar
 * una liquidación de jornadas.
 * </p>
 */
@Getter
@Setter
@View(members = "periodoDesde; periodoHasta")
public class PeriodoLiquidacionDialog {

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

    /**
     * Calculador para obtener el primer día del mes actual.
     */
    public static class InicioMesActualCalculator implements org.openxava.calculators.ICalculator {
        @Override
        public Object calculate() throws Exception {
            return LocalDate.now().withDayOfMonth(1);
        }
    }

    /**
     * Calculador para obtener el último día del mes actual.
     */
    public static class FinMesActualCalculator implements org.openxava.calculators.ICalculator {
        @Override
        public Object calculate() throws Exception {
            LocalDate hoy = LocalDate.now();
            return hoy.withDayOfMonth(hoy.lengthOfMonth());
        }
    }
}
