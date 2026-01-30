package com.sta.biometric.acciones;

import org.openxava.actions.*;
import org.openxava.jpa.*;

import com.sta.biometric.modelo.*;
import com.sta.biometric.servicios.RedondeoHorasService;
import com.sta.biometric.servicios.LiquidacionJornadaService;

/**
 * Acción que revierte los ajustes de redondeo de una liquidación.
 * 
 * @author Sistema STARH
 * @since 2.0
 */
public class RevertirRedondeoMasivoAction extends ViewBaseAction {

    @Override
    public void execute() throws Exception {
        // Obtener entidad de la vista
        Object entity = getView().getEntity();
        if (!(entity instanceof LiquidacionJornadas)) {
            addError("Esta acción solo puede ejecutarse desde una Liquidación de Jornadas.");
            return;
        }

        LiquidacionJornadas liquidacion = (LiquidacionJornadas) entity;

        // Revertir redondeo de todos los registros
        int revertidos = RedondeoHorasService.revertirRedondeo(liquidacion);

        if (revertidos > 0) {
            // Recalcular liquidación
            LiquidacionJornadaService.recalcularLiquidacion(liquidacion);

            // Commit cambios
            XPersistence.commit();

            addMessage("🔄 Redondeo revertido en " + revertidos + " registro(s). Liquidación recalculada.");
        } else {
            addMessage("ℹ️ No había ajustes de redondeo para revertir.");
        }

        // Refrescar vista
        getView().refresh();
    }
}
