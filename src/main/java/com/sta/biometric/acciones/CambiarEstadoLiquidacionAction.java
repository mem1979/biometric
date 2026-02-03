package com.sta.biometric.acciones;

import org.openxava.actions.*;
import org.openxava.jpa.*;
import org.openxava.model.*;

import com.sta.biometric.enums.*;
import com.sta.biometric.modelo.*;

/**
 * Acción para cambiar el estado de una liquidación (Abrir/Cerrar).
 * 
 * <p>
 * Funciona como un toggle:
 * <ul>
 * <li>Si está ABIERTO/RECALCULADO -> Cierra</li>
 * <li>Si está CERRADO -> Abre</li>
 * </ul>
 * </p>
 * 
 * @author Sistema STARH
 */
public class CambiarEstadoLiquidacionAction extends ViewBaseAction {

    @Override
    public void execute() throws Exception {
        // Obtener la entidad actual
        LiquidacionJornadas liquidacion = (LiquidacionJornadas) MapFacade.findEntity(
                getModelName(),
                getView().getKeyValues());

        if (liquidacion == null) {
            addError("No se encontró la liquidación");
            return;
        }

        try {
            EstadoLiquidacion estadoActual = liquidacion.getEstadoPeriodo();

            if (estadoActual == EstadoLiquidacion.CERRADO) {
                // ABRIR
                liquidacion.abrir();
                addMessage("Liquidación REABIERTA exitosamente");
            } else {
                // CERRAR
                liquidacion.cerrar();
                addMessage("Liquidación CERRADA exitosamente");
            }

            // Guardar cambios
            XPersistence.commit();

            // Refrescar vista
            getView().refresh();

        } catch (Exception e) {
            addError("Error al cambiar estado: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
