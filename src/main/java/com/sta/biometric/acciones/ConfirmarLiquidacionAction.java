package com.sta.biometric.acciones;

import java.time.*;

import org.openxava.actions.*;
import org.openxava.jpa.*;

import com.sta.biometric.modelo.*;
import com.sta.biometric.servicios.*;

/**
 * Acción que genera la liquidación con el período seleccionado por el usuario.
 * 
 * <p>
 * Se ejecuta desde el diálogo de selección de período. Recupera el empleado
 * del contexto y genera la liquidación para las fechas especificadas.
 * </p>
 * 
 * @author Sistema STARH
 * @since 2.0
 */
public class ConfirmarLiquidacionAction extends ViewBaseAction {

    @Override
    public void execute() throws Exception {
        // Obtener fechas del diálogo
        LocalDate periodoDesde = (LocalDate) getView().getValue("periodoDesde");
        LocalDate periodoHasta = (LocalDate) getView().getValue("periodoHasta");

        // Validar fechas
        if (periodoDesde == null || periodoHasta == null) {
            addError("Debe especificar ambas fechas del período");
            return;
        }

        if (periodoDesde.isAfter(periodoHasta)) {
            addError("La fecha de inicio debe ser anterior o igual a la fecha de fin");
            return;
        }

        // Recuperar el ID del empleado guardado en el contexto
        String empleadoId = (String) getContext().get(getRequest(), "liquidacion_empleado_id");

        if (empleadoId == null || empleadoId.isEmpty()) {
            addError("No se pudo recuperar el empleado. Por favor, inténtelo de nuevo.");
            closeDialog();
            return;
        }

        try {
            // Buscar el empleado
            Personal empleado = XPersistence.getManager().find(Personal.class, empleadoId);

            if (empleado == null) {
                addError("No se encontró el empleado");
                closeDialog();
                return;
            }

            // Generar liquidación
            LiquidacionJornadas liquidacion = LiquidacionJornadaService
                    .generarLiquidacion(empleado, periodoDesde, periodoHasta);

            XPersistence.commit();

            addMessage("Liquidación generada exitosamente para el período " +
                    periodoDesde + " - " + periodoHasta);
            addMessage("Total horas normales: " + liquidacion.getHorasNormalesFormatted());
            addMessage("Total horas extras: " + liquidacion.getHorasExtrasFormatted());
            addMessage("Total horas especiales: " + liquidacion.getHorasEspecialesFormatted());
            addMessage("Gran total: $" + liquidacion.getMontoGranTotal());

            // Cerrar diálogo y refrescar vista principal
            closeDialog();
            getPreviousView().refresh();

        } catch (IllegalArgumentException e) {
            addError(e.getMessage());
        } catch (Exception e) {
            addError("Error al generar liquidación: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
