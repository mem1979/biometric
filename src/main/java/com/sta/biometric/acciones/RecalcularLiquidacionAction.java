package com.sta.biometric.acciones;

import org.openxava.actions.*;
import org.openxava.jpa.*;
import org.openxava.model.*;

import com.sta.biometric.enums.*;
import com.sta.biometric.modelo.*;
import com.sta.biometric.servicios.*;

/**
 * Acción para recalcular una liquidación de jornadas existente.
 * 
 * <p>
 * Reconsulta AuditoriaRegistros y actualiza las horas y montos.
 * </p>
 * 
 * @author Sistema STARH
 * @since 2.0
 */
public class RecalcularLiquidacionAction extends ViewBaseAction {

    @Override
    public void execute() throws Exception {
        // Obtener la liquidación actual
        LiquidacionJornadas liquidacion = (LiquidacionJornadas) MapFacade.findEntity(
                getView().getModelName(),
                getView().getKeyValues());

        if (liquidacion == null) {
            addError("No se encontró la liquidación");
            return;
        }

        if (liquidacion.getEstadoPeriodo() == EstadoLiquidacion.CERRADO) {
            addWarning("No se puede recalcular una liquidación cerrada");
            return;
        }

        try {
            // Valores anteriores para comparación
            int minutosNormalesAntes = liquidacion.getTotalMinutosNormales();
            int minutosExtrasAntes = liquidacion.getTotalMinutosExtras();
            int minutosEspecialesAntes = liquidacion.getTotalMinutosEspeciales();

            // Recalcular
            LiquidacionJornadaService.recalcularLiquidacion(liquidacion);

            XPersistence.commit();

            // Mostrar diferencias
            int difNormales = liquidacion.getTotalMinutosNormales() - minutosNormalesAntes;
            int difExtras = liquidacion.getTotalMinutosExtras() - minutosExtrasAntes;
            int difEspeciales = liquidacion.getTotalMinutosEspeciales() - minutosEspecialesAntes;

            addMessage("Liquidación recalculada exitosamente");

            if (difNormales != 0 || difExtras != 0 || difEspeciales != 0) {
                addMessage("Diferencias encontradas:");
                if (difNormales != 0) {
                    addMessage("  Normales: " + (difNormales > 0 ? "+" : "") + difNormales + " min");
                }
                if (difExtras != 0) {
                    addMessage("  Extras: " + (difExtras > 0 ? "+" : "") + difExtras + " min");
                }
                if (difEspeciales != 0) {
                    addMessage("  Especiales: " + (difEspeciales > 0 ? "+" : "") + difEspeciales + " min");
                }
            } else {
                addMessage("No se encontraron diferencias en las horas");
            }

            addMessage("Nuevo gran total: $" + liquidacion.getMontoGranTotal());

            // Refrescar la vista
            getView().refresh();

        } catch (IllegalStateException e) {
            addError(e.getMessage());
        } catch (Exception e) {
            addError("Error al recalcular: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
