package com.sta.biometric.acciones;

import java.time.*;

import org.openxava.actions.*;

import com.sta.biometric.servicios.*;
import com.sta.biometric.servicios.GestionJornadasService.ReprocesarResultado;

/**
 * Ejecuta la lógica de reevaluación tomando los datos del diálogo.
 */
public class EjecutarReevaluacionAction extends ViewBaseAction {

    @Override
    public void execute() throws Exception {
        LocalDate fechaDesde = (LocalDate) getView().getValue("fechaDesde");
        LocalDate fechaHasta = (LocalDate) getView().getValue("fechaHasta");

        // Validación básica
        if (fechaDesde == null || fechaHasta == null) {
            addError("Debe especificar fecha desde y hasta.");
            return;
        }

        if (fechaHasta.isBefore(fechaDesde)) {
            addError("Fecha hasta debe ser posterior a fecha desde.");
            return;
        }

        long dias = Duration.between(fechaDesde.atStartOfDay(), fechaHasta.atStartOfDay()).toDays();
        if (dias > 31) {
            addWarning("Procesando periodo largo (" + dias + " días). Puede demorar.");
        }

        try {
            // Null employee means ALL active employees
            ReprocesarResultado resultado = GestionJornadasService.getInstance()
                    .reprocesarPeriodo(fechaDesde, fechaHasta, null);

            // Mostrar resultado
            addMessage("Reevaluación completada: " + resultado.jornadasProcesadas + " jornadas procesadas.");

            if (resultado.tieneErrores()) {
                addWarning("Se encontraron " + resultado.errores + " errores durante el proceso.");
                // Mostrar hasta 5 errores para no saturar
                int mostrados = 0;
                for (String detalle : resultado.detallesErrores) {
                    if (mostrados++ >= 5) {
                        addWarning("... y " + (resultado.errores - 5) + " errores más.");
                        break;
                    }
                    addWarning("- " + detalle);
                }
            }

            closeDialog();
        } catch (Exception e) {
            addError("Error durante la reevaluación: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
