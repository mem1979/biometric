package com.sta.biometric.acciones;

import java.time.*;

import org.openxava.actions.*;
import org.openxava.jpa.*;
import org.openxava.model.*;

import com.sta.biometric.modelo.*;
import com.sta.biometric.servicios.*;

/**
 * Acción para generar una liquidación de jornadas desde la vista de Personal.
 * 
 * <p>
 * Genera una liquidación para el mes actual del empleado seleccionado.
 * </p>
 * 
 * @author Sistema STARH
 * @since 2.0
 */
public class GenerarLiquidacionAction extends ViewBaseAction {

    @Override
    public void execute() throws Exception {
        // Obtener el empleado actual
        Personal empleado = (Personal) MapFacade.findEntity(
                getView().getModelName(),
                getView().getKeyValues());

        if (empleado == null) {
            addError("No se encontró el empleado");
            return;
        }

        // Calcular período del mes actual
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        LocalDate finMes = hoy.withDayOfMonth(hoy.lengthOfMonth());

        try {
            // Generar liquidación
            LiquidacionJornadas liquidacion = LiquidacionJornadaService
                    .generarLiquidacion(empleado, inicioMes, finMes);

            XPersistence.commit();

            addMessage("Liquidación generada exitosamente para el período " +
                    inicioMes + " - " + finMes);
            addMessage("Total horas normales: " + liquidacion.getHorasNormalesFormatted());
            addMessage("Total horas extras: " + liquidacion.getHorasExtrasFormatted());
            addMessage("Total horas especiales: " + liquidacion.getHorasEspecialesFormatted());
            addMessage("Gran total: $" + liquidacion.getMontoGranTotal());

            // Refrescar la vista para mostrar la nueva liquidación
            getView().refresh();

        } catch (IllegalArgumentException e) {
            addError(e.getMessage());
        } catch (Exception e) {
            addError("Error al generar liquidación: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
