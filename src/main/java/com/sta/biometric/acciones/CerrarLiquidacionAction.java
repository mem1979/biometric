package com.sta.biometric.acciones;



import org.openxava.actions.*;
import org.openxava.jpa.*;
import org.openxava.model.*;

import com.sta.biometric.enums.*;
import com.sta.biometric.modelo.*;

/**
 * Acción para cerrar una liquidación de jornadas.
 * 
 * <p>
 * Marca la liquidación como CERRADA, impidiendo modificaciones futuras.
 * </p>
 * 
 * @author Sistema STARH
 * @since 2.0
 */
public class CerrarLiquidacionAction extends ViewBaseAction {

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
            addWarning("La liquidación ya está cerrada");
            return;
        }

        try {
            // Cerrar la liquidación
            liquidacion.cerrar();

            XPersistence.commit();

            addMessage("Liquidación cerrada exitosamente");
            addMessage("Período: " + liquidacion.getPeriodoDesde() + " - " + liquidacion.getPeriodoHasta());
            addMessage("Gran total final: $" + liquidacion.getMontoGranTotal());

            // Refrescar la vista
            getView().refresh();

        } catch (Exception e) {
            addError("Error al cerrar liquidación: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
