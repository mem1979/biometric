package com.sta.biometric.acciones;

import java.util.List;

import org.openxava.actions.CollectionBaseAction;
import org.openxava.jpa.XPersistence;

import com.sta.biometric.enums.EstadoLiquidacion;
import com.sta.biometric.modelo.LiquidacionJornadas;
import com.sta.biometric.servicios.LiquidacionJornadaService;

/**
 * Acción para recalcular una liquidación de jornadas desde la lista de la
 * colección.
 * 
 * <p>
 * Funciona tanto desde @RowAction (fila específica) como @ListAction
 * (selección).
 * Reconsulta AuditoriaRegistros y actualiza las horas y montos.
 * </p>
 * 
 * @author Sistema STARH
 * @since 2.1
 */
public class RecalcularLiquidacionAction extends CollectionBaseAction {

    @Override
    public void execute() throws Exception {
        // Obtener el objeto seleccionado (funciona para @RowAction y @ListAction)
        List<?> selectedObjects = getSelectedObjects();

        if (selectedObjects == null || selectedObjects.isEmpty()) {
            addError("Debe seleccionar una liquidación");
            return;
        }

        // Tomamos el primer elemento seleccionado
        LiquidacionJornadas liquidacion = (LiquidacionJornadas) selectedObjects.get(0);

        if (liquidacion == null) {
            addError("No se pudo cargar la liquidación seleccionada");
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

            // Refrescar la colección
            getCollectionElementView().getCollectionTab().reset();

        } catch (IllegalStateException e) {
            addError(e.getMessage());
        } catch (Exception e) {
            addError("Error al recalcular: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
